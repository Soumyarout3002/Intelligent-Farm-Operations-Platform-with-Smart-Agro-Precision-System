package com.backend.dto.inventory;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150, message = "Supplier name cannot exceed 150 characters")
    private String name;

    @Size(max = 150)
    private String contactPerson;

    @Size(max = 20)
    private String phone;

    @Email(message = "Please provide a valid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 255)
    private String address;
}