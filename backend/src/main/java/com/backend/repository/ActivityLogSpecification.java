package com.backend.repository;

import com.backend.entity.ActivityLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> filter(
            Long userId,
            String actionType,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, criteriaBuilder) -> {

            var predicate = criteriaBuilder.conjunction();

            if (userId != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                root.get("actorUserId"), userId));
            }

            if (actionType != null && !actionType.isBlank()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("actionType")),
                                actionType.trim().toUpperCase()));
            }

            if (startDate != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("createdAt"), endDate));
            }

            return predicate;
        };
    }
}