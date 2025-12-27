package ru.effectivemobile.bankcards.repository;

import ru.effectivemobile.bankcards.entity.Role;
import ru.effectivemobile.bankcards.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("$2a$10$fake");
        user.setRole(Role.USER);

        // when
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}