package com.cafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    // Note: role is intentionally NOT accepted here for public registration.
    // Public signups always become USER. WORKER/ADMIN accounts are created
    // by an admin via /api/admin/users - see AdminController.
}
