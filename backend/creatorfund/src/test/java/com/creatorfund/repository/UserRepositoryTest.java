package com.creatorfund.repository;

import com.creatorfund.model.User;
import com.creatorfund.model.UserRole;
import com.creatorfund.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        user.setRole(UserRole.CREATOR);
        user.setStatus(UserStatus.ACTIVE);

        // When
        userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFullName()).isEqualTo("Test User");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.CREATOR);
    }

    @Test
    void shouldCheckEmailExists() {
        // Given
        User user = new User();
        user.setEmail("exists@example.com");
        user.setFullName("Existing User");
        user.setPasswordHash("hashedPassword");
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }
}
