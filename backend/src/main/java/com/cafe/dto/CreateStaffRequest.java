package com.cafe.dto;

import com.cafe.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Used by ADMIN to create WORKER or ADMIN accounts
@Data
public class CreateStaffRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role; // WORKER or ADMIN
}
