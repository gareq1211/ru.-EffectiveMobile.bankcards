package ru.effectivemobile.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.effectivemobile.bankcards.dto.CreateUserRequest;
import ru.effectivemobile.bankcards.dto.UpdateUserRequest;
import ru.effectivemobile.bankcards.dto.UserDto;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        return userRepository.save(user);
    }

    // ✅ Получить всех пользователей (ADMIN only)
    public List<UserDto> getAllUsers() {
        checkAdminAccess();

        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Получить пользователей с пагинацией (ADMIN only)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        checkAdminAccess();

        return userRepository.findAll(pageable)
                .map(this::toDto);
    }

    // ✅ Получить пользователя по ID (ADMIN или сам пользователь)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверяем права доступа
        checkUserAccess(userId);

        return toDto(user);
    }

    // ✅ Обновить пользователя (ADMIN или сам пользователь для своего профиля)
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверяем права доступа
        checkUserAccess(userId);

        // Обновляем поля если они предоставлены
        if (request.email() != null && !request.email().isBlank()) {
            if (!request.email().equals(user.getEmail()) &&
                    userRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        // Только ADMIN может менять роль
        if (request.role() != null && isAdmin()) {
            user.setRole(request.role());
        }

        User updatedUser = userRepository.save(user);
        return toDto(updatedUser);
    }

    // ✅ Удалить пользователя (ADMIN only)
    @Transactional
    public void deleteUser(Long userId) {
        checkAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверяем, что пользователь не удаляет сам себя
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        userRepository.delete(user);
    }

    // ✅ Получить текущего пользователя
    public UserDto getCurrentUser() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        return toDto(user);
    }

    // Вспомогательные методы
    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getRole().name());
    }

    private void checkAdminAccess() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    private void checkUserAccess(Long userId) {
        if (isAdmin()) {
            return; // ADMIN имеет доступ ко всем пользователям
        }

        // USER может доступ только к своему профилю
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to user with id: " + userId);
        }
    }

    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}