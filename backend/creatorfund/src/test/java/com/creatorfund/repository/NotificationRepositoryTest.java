package com.creatorfund.repository;

import com.creatorfund.model.Notification;
import com.creatorfund.model.NotificationType;
import com.creatorfund.model.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUnreadNotifications() {
        // Given
        User user = createTestUser("user@test.com");

        Notification notification1 = new Notification();
        notification1.setUser(user);
        notification1.setType(NotificationType.UPDATE);
        notification1.setContent("Test notification 1");
        notification1.setReadStatus(false);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setUser(user);
        notification2.setType(NotificationType.COMMENT);
        notification2.setContent("Test notification 2");
        notification2.setReadStatus(true);
        notificationRepository.save(notification2);

        // When
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndReadStatus(user.getId(), false);

        // Then
        assertThat(unreadNotifications).hasSize(1);
        assertThat(unreadNotifications.get(0).getContent())
                .isEqualTo("Test notification 1");
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }
}
