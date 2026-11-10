package com.backend.controller;

import com.backend.dto.equipment.EquipmentBookingRequest;
import com.backend.dto.equipment.EquipmentBookingResponse;
import com.backend.entity.EquipmentBooking;
import com.backend.entity.User;
import com.backend.service.EquipmentBookingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/equipment/bookings")
@RequiredArgsConstructor
public class EquipmentBookingController {

    private final EquipmentBookingService bookingService;

    @PostMapping
    public ResponseEntity<EquipmentBookingResponse> createBooking(
            @PathVariable Long farmId,
            @Valid @RequestBody EquipmentBookingRequest request,
            HttpServletRequest httpRequest
    ) {
        User farmer = getAuthenticatedUser(httpRequest);

        EquipmentBooking booking = bookingService.createBooking(
                farmId,
                request.getEquipmentId(),
                farmer,
                request.getBookingDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(booking));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentBookingResponse>> getBookings(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest
    ) {
        User user = getAuthenticatedUser(httpRequest);

        List<EquipmentBookingResponse> bookings =
                bookingService.getBookings(farmId, user)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{bookingId}/approve")
    public ResponseEntity<EquipmentBookingResponse> approveBooking(
            @PathVariable Long farmId,
            @PathVariable Long bookingId,
            HttpServletRequest httpRequest
    ) {
        User admin = getAuthenticatedUser(httpRequest);

        EquipmentBooking booking = bookingService.approveBooking(
                farmId, bookingId, admin
        );

        return ResponseEntity.ok(toResponse(booking));
    }

    @PatchMapping("/{bookingId}/reject")
    public ResponseEntity<EquipmentBookingResponse> rejectBooking(
            @PathVariable Long farmId,
            @PathVariable Long bookingId,
            HttpServletRequest httpRequest
    ) {
        User admin = getAuthenticatedUser(httpRequest);

        EquipmentBooking booking = bookingService.rejectBooking(
                farmId, bookingId, admin
        );

        return ResponseEntity.ok(toResponse(booking));
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return user;
    }

    private EquipmentBookingResponse toResponse(
            EquipmentBooking booking
    ) {
        return EquipmentBookingResponse.builder()
                .id(booking.getId())
                .equipmentId(booking.getEquipment().getId())
                .equipment(booking.getEquipment().getName())
                .farmerId(booking.getFarmer().getId())
                .farmer(booking.getFarmer().getName())
                .date(booking.getBookingDate())
                .status(booking.getStatus())
                .reviewedById(
                        booking.getReviewedBy() != null
                                ? booking.getReviewedBy().getId()
                                : null
                )
                .reviewedBy(
                        booking.getReviewedBy() != null
                                ? booking.getReviewedBy().getName()
                                : null
                )
                .reviewedAt(booking.getReviewedAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}