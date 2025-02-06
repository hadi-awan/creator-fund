package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateNotificationRequest;
import com.creatorfund.dto.response.NotificationResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.NotificationMapper;
import com.creatorfund.model.Notification;
import com.creatorfund.model.NotificationType;
import com.creatorfund.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest extends BaseServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                notificationMapper
        );
    }

    @Test
    void createNotification_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String content = "Test notification";
        String type = NotificationType.UPDATE.name();  // Use actual enum value
        UUID referenceId = UUID.randomUUID();

        Notification notification = createSampleNotification();
        NotificationResponse expectedResponse = createSampleNotificationResponse();

        when(notificationMapper.toEntity(any(CreateNotificationRequest.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(expectedResponse);

        // Act
        notificationService.createNotification(userId, content, type, referenceId);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toResponse(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = createSampleNotification();
        notification.setReadStatus(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        assertThat(notification.isReadStatus()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_NotificationNotFound() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found");
    }

    @Test
    void getUnreadNotifications_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<Notification> notifications = Arrays.asList(
                createSampleNotification(),
                createSampleNotification()
        );
        List<NotificationResponse> expectedResponses = Arrays.asList(
                createSampleNotificationResponse(),
                createSampleNotificationResponse()
        );

        when(notificationRepository.findByUserIdAndReadStatus(userId, false))
                .thenReturn(notifications);
        when(notificationMapper.toResponse(any(Notification.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        // Act
        List<NotificationResponse> result = notificationService.getUnreadNotifications(userId);

        // Assert
        assertThat(result).hasSize(2);
        verify(notificationRepository).findByUserIdAndReadStatus(userId, false);
        verify(notificationMapper, times(2)).toResponse(any(Notification.class));
    }

    // Helper methods
    private Notification createSampleNotification() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setContent("Test notification");
        notification.setType(NotificationType.UPDATE);  // Use actual enum value
        notification.setReferenceId(UUID.randomUUID());
        notification.setReadStatus(false);
        notification.setCreatedAt(ZonedDateTime.now());
        return notification;
    }

    private NotificationResponse createSampleNotificationResponse() {
        return NotificationResponse.builder()
                .id(UUID.randomUUID())
                .content("Test notification")
                .type(NotificationType.UPDATE.name())  // Use actual enum value
                .referenceId(UUID.randomUUID())
                .readStatus(false)
                .createdAt(ZonedDateTime.now())
                .build();
    }
}