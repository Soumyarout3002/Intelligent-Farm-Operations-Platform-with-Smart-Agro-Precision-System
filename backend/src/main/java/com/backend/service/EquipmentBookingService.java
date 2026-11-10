package com.backend.service;

import com.backend.entity.Equipment;
import com.backend.entity.EquipmentBooking;
import com.backend.entity.Farm;
import com.backend.entity.User;
import com.backend.repository.EquipmentBookingRepository;
import com.backend.repository.EquipmentRepository;
import com.backend.repository.FarmRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentBookingService {

    private final EquipmentBookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final FarmRepository farmRepository;

    public EquipmentBooking createBooking(
            Long farmId,
            Long equipmentId,
            User farmer,
            java.time.LocalDate bookingDate
    ) {
        if (bookingDate == null || bookingDate.isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking date cannot be in the past"
            );
        }
        Farm farm = farmRepository.findByIdAndOwnerId(
                farmId, farmer.getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Farm not found"
        ));

        Equipment equipment = equipmentRepository
                .findByIdAndFarmId(equipmentId, farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Equipment not found"
                ));

        if (!"Available".equalsIgnoreCase(equipment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Equipment is not available for booking"
            );
        }

        EquipmentBooking booking = EquipmentBooking.builder()
                .equipment(equipment)
                .farmer(farmer)
                .bookingDate(bookingDate)
                .status("Pending")
                .build();

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<EquipmentBooking> getBookings(
            Long farmId,
            User user
    ) {
        verifyFarmAccess(farmId, user);

        return bookingRepository
                .findByEquipmentFarmIdOrderByBookingDateAsc(farmId);
    }

    public EquipmentBooking approveBooking(
            Long farmId,
            Long bookingId,
            User admin
    ) {
        verifyAdmin(admin);

        EquipmentBooking booking = getBooking(farmId, bookingId);

        // Lock the equipment row to serialize approval attempts.
        Equipment equipment = bookingRepository
                .findEquipmentForUpdate(
                        booking.getEquipment().getId(), farmId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Equipment not found"
                ));

        if ("Approved".equalsIgnoreCase(booking.getStatus())) {
            return booking;
        }

        if (!"Pending".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending bookings can be approved"
            );
        }

        boolean alreadyBooked = bookingRepository
                .existsByEquipmentIdAndBookingDateAndStatusIgnoreCaseAndIdNot(
                        equipment.getId(),
                        booking.getBookingDate(),
                        "Approved",
                        booking.getId()
                );

        if (alreadyBooked) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Equipment is already booked for this date"
            );
        }

        booking.setStatus("Approved");
        booking.setReviewedBy(admin);
        booking.setReviewedAt(java.time.LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public EquipmentBooking rejectBooking(
            Long farmId,
            Long bookingId,
            User admin
    ) {
        verifyAdmin(admin);

        EquipmentBooking booking = getBooking(farmId, bookingId);

        if (!"Pending".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending bookings can be rejected"
            );
        }

        booking.setStatus("Rejected");
        booking.setReviewedBy(admin);
        booking.setReviewedAt(java.time.LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    private EquipmentBooking getBooking(
            Long farmId,
            Long bookingId
    ) {
        if (!farmRepository.existsById(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Farm not found"
            );
        }

        return bookingRepository
                .findByIdAndEquipmentFarmId(bookingId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));
    }

    private void verifyFarmAccess(Long farmId, User user) {
        if (isAdmin(user)) {
            if (!farmRepository.existsById(farmId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"
                );
            }
            return;
        }

        if (!farmRepository.findByIdAndOwnerId(
                farmId, user.getId()
        ).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Farm not found"
            );
        }
    }

    private void verifyAdmin(User user) {
        if (!isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can review bookings"
            );
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && "ADMIN".equalsIgnoreCase(
                        user.getRole().trim()
                );
    }
}