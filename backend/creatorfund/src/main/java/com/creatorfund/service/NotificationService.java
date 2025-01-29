package com.creatorfund.service;

import com.creatorfund.dto.request.CreateNotificationRequest;
import com.creatorfund.dto.response.NotificationResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.NotificationMapper;
import com.creatorfund.model.Notification;
import com.creatorfund.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public void createNotification(UUID userId, String content,
                                   String type, UUID referenceId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setContent(content);
        request.setType(type);
        request.setReferenceId(referenceId);

        Notification notification = notificationMapper.toEntity(request);
        notification = notificationRepository.save(notification);
        notificationMapper.toResponse(notification);
    }

    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndReadStatus(userId, false)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
