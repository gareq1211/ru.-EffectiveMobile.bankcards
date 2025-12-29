package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import ru.effectivemobile.bankcards.entity.Role;

public record UpdateUserRequest(
        @Email
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        Role role
) {}