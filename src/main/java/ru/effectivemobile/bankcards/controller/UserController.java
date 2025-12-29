package ru.effectivemobile.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ru.effectivemobile.bankcards.dto.CreateUserRequest;
import ru.effectivemobile.bankcards.dto.UpdateUserRequest;
import ru.effectivemobile.bankcards.dto.UserDto;
import ru.effectivemobile.bankcards.service.UserService;

import java.util.List;

@Tag(name = "Users", description = "Manage users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ Создание пользователя (ADMIN only)
    @Operation(summary = "Create a new user (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "User created")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        var user = userService.createUser(request);
        var userDto = new UserDto(user.getId(), user.getEmail(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    // ✅ Получить всех пользователей (ADMIN only)
    @Operation(summary = "Get all users (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ✅ Получить пользователей с пагинацией (ADMIN only)
    @Operation(summary = "Get users with pagination (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved")
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsersPaged(Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    // ✅ Получить пользователя по ID
    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User retrieved")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ✅ Получить текущего пользователя
    @Operation(summary = "Get current user")
    @ApiResponse(responseCode = "200", description = "User retrieved")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    // ✅ Обновить пользователя
    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    // ✅ Удалить пользователя (ADMIN only)
    @Operation(summary = "Delete user (ADMIN only)")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}