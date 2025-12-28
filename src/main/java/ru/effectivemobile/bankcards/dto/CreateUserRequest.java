package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ru.effectivemobile.bankcards.entity.Role;

public record CreateUserRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        Role role
) {}