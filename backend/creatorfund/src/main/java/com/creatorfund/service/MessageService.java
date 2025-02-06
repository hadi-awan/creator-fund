package com.creatorfund.service;

import com.creatorfund.dto.request.CreateMessageRequest;
import com.creatorfund.dto.response.MessageResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.MessageMapper;
import com.creatorfund.model.Message;
import com.creatorfund.model.Project;
import com.creatorfund.model.User;
import com.creatorfund.repository.MessageRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MessageMapper messageMapper;
    private final NotificationService notificationService;

    public MessageResponse sendMessage(CreateMessageRequest request, UUID senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            validateProjectAccess(project, sender, recipient);
        }

        Message message = messageMapper.toEntity(request);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setProject(project);

        Message savedMessage = messageRepository.save(message);

        // Notify recipient
        notifyNewMessage(savedMessage);

        return messageMapper.toResponse(savedMessage);
    }

    public Page<MessageResponse> getConversations(UUID userId, Pageable pageable) {
        return messageRepository.findUserConversations(userId, pageable)
                .map(messageMapper::toResponse);
    }

    public List<MessageResponse> getConversationMessages(UUID userId, UUID otherUserId) {
        validateUsers(userId, otherUserId);
        return messageRepository.findConversation(userId, otherUserId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public List<MessageResponse> getProjectMessages(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectMembership(project, userId);

        return messageRepository.findByProjectId(projectId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public void markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getRecipient().getId().equals(userId)) {
            throw new BusinessValidationException("Not authorized to mark this message as read");
        }

        message.setReadStatus(true);
        messageRepository.save(message);
    }

    public void markConversationAsRead(UUID userId, UUID otherUserId) {
        validateUsers(userId, otherUserId);
        messageRepository.markConversationAsRead(userId, otherUserId);
    }

    private void validateUsers(UUID userId, UUID otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BusinessValidationException("Cannot message yourself");
        }

        if (!userRepository.existsById(otherUserId)) {
            throw new ResourceNotFoundException("Other user not found");
        }
    }

    private void validateProjectAccess(Project project, User sender, User recipient) {
        boolean senderAccess = project.getCreator().getId().equals(sender.getId()) ||
                project.getProjectTeam().stream()
                        .anyMatch(member -> member.getUser().getId().equals(sender.getId()));

        boolean recipientAccess = project.getCreator().getId().equals(recipient.getId()) ||
                project.getProjectTeam().stream()
                        .anyMatch(member -> member.getUser().getId().equals(recipient.getId()));

        if (!senderAccess || !recipientAccess) {
            throw new BusinessValidationException("One or both users do not have access to this project");
        }
    }

    private void validateProjectMembership(Project project, UUID userId) {
        boolean isMember = project.getCreator().getId().equals(userId) ||
                project.getProjectTeam().stream()
                        .anyMatch(member -> member.getUser().getId().equals(userId));

        if (!isMember) {
            throw new BusinessValidationException("User is not a member of this project");
        }
    }

    private void notifyNewMessage(Message message) {
        notificationService.createNotification(
                message.getRecipient().getId(),
                String.format("New message from %s", message.getSender().getFullName()),
                "NEW_MESSAGE",
                message.getId()
        );
    }
}
