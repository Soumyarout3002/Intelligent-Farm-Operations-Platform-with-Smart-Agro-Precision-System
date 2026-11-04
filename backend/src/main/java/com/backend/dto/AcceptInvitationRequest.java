
package com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(

        @NotBlank
        String token,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
