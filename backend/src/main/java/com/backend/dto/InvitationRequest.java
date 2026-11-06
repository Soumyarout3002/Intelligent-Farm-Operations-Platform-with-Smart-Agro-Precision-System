package com.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InvitationRequest(

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Pattern(
            regexp = "(?i)ADMIN|FARMER|FIELD_WORKER",
            message = "Only ADMIN, FARMER and FIELD_WORKER roles can be invited."
        )
        String role
) {
}