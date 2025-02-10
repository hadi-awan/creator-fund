package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateMessageRequest;
import com.creatorfund.dto.response.MessageResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.MessageMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.MessageRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageServiceTest extends BaseServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private NotificationService notificationService;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                messageRepository,
                userRepository,
                projectRepository,
                messageMapper,
                notificationService
        );
    }

    @Test
    void sendMessage_Success() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        CreateMessageRequest request = createSampleMessageRequest();
        User sender = createSampleUser();
        User recipient = createSampleUser();
        Message message = createSampleMessage(sender, recipient);
        MessageResponse expectedResponse = createSampleMessageResponse();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(request.getRecipientId())).thenReturn(Optional.of(recipient));
        when(messageMapper.toEntity(request)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);
        doNothing().when(notificationService).createNotification(any(), any(), any(), any());

        // Act
        MessageResponse result = messageService.sendMessage(request, senderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(expectedResponse.getContent());
        verify(messageRepository).save(any(Message.class));
        verify(notificationService).createNotification(any(), any(), any(), any());
    }

    @Test
    void sendMessage_WithProject_Success() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        CreateMessageRequest request = createSampleMessageRequestWithProject();
        User sender = createSampleUser();
        User recipient = createSampleUser();
        User projectCreator = createSampleUser();

        // Create project with team members
        Project project = createSampleProject(projectCreator);

        // Add sender to project team
        ProjectTeam senderTeam = new ProjectTeam();
        senderTeam.setUser(sender);
        senderTeam.setRole(TeamRole.CONTRIBUTOR);
        project.getProjectTeam().add(senderTeam);

        // Add recipient to project team
        ProjectTeam recipientTeam = new ProjectTeam();
        recipientTeam.setUser(recipient);
        recipientTeam.setRole(TeamRole.CONTRIBUTOR);
        project.getProjectTeam().add(recipientTeam);

        Message message = createSampleMessage(sender, recipient, project);
        MessageResponse expectedResponse = createSampleMessageResponseWithProject(project.getId());

        // Set request IDs to match test data
        request.setRecipientId(recipient.getId());
        request.setProjectId(project.getId());

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(request.getRecipientId())).thenReturn(Optional.of(recipient));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(messageMapper.toEntity(request)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        // Act
        MessageResponse result = messageService.sendMessage(request, senderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProjectId()).isEqualTo(project.getId());
    }

    @Test
    void sendMessage_RecipientNotFound() {
        // Arrange
        UUID senderId = UUID.randomUUID();
        CreateMessageRequest request = createSampleMessageRequest();
        User sender = createSampleUser();

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(request.getRecipientId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.sendMessage(request, senderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recipient not found");
    }

    @Test
    void getConversation_Success() {
        // Arrange
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        User user1 = createSampleUser();
        User user2 = createSampleUser();
        List<Message> messages = Arrays.asList(
                createSampleMessage(user1, user2),
                createSampleMessage(user2, user1)
        );
        List<MessageResponse> expectedResponses = Arrays.asList(
                createSampleMessageResponse(),
                createSampleMessageResponse()
        );

        // Add this mock for user validation
        when(userRepository.existsById(user2Id)).thenReturn(true);

        when(messageRepository.findConversation(user1Id, user2Id)).thenReturn(messages);
        when(messageMapper.toResponse(any(Message.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        // Act
        List<MessageResponse> result = messageService.getConversationMessages(user1Id, user2Id);

        // Assert
        assertThat(result).hasSize(2);
        verify(messageRepository).findConversation(user1Id, user2Id);
    }

    @Test
    void markAsRead_Success() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Message message = createSampleMessage(createSampleUser(), createSampleUser());
        message.getRecipient().setId(userId);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        // Act
        messageService.markAsRead(messageId, userId);

        // Assert
        assertThat(message.isReadStatus()).isTrue();
        verify(messageRepository).save(message);
    }

    @Test
    void markAsRead_NotAuthorized() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Message message = createSampleMessage(createSampleUser(), createSampleUser());

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // Act & Assert
        assertThatThrownBy(() -> messageService.markAsRead(messageId, userId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Not authorized to mark this message as read");
    }

    // Helper methods
    private CreateMessageRequest createSampleMessageRequest() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setContent("Test message");
        request.setRecipientId(UUID.randomUUID());
        return request;
    }

    private CreateMessageRequest createSampleMessageRequestWithProject() {
        CreateMessageRequest request = createSampleMessageRequest();
        request.setProjectId(UUID.randomUUID());
        return request;
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        return user;
    }

    private Project createSampleProject(User creator) {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test Project");
        project.setCreator(creator);
        project.setProjectTeam(new ArrayList<>());
        return project;
    }

    private Message createSampleMessage(User sender, User recipient) {
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent("Test message");
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setCreatedAt(ZonedDateTime.now());
        message.setReadStatus(false);
        return message;
    }

    private Message createSampleMessage(User sender, User recipient, Project project) {
        Message message = createSampleMessage(sender, recipient);
        message.setProject(project);
        return message;
    }

    private MessageResponse createSampleMessageResponse() {
        return MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Test message")
                .senderId(UUID.randomUUID())
                .recipientId(UUID.randomUUID())
                .readStatus(false)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    private MessageResponse createSampleMessageResponseWithProject(UUID projectId) {
        return MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Test message")
                .senderId(UUID.randomUUID())
                .recipientId(UUID.randomUUID())
                .projectId(projectId)  // Include project ID
                .readStatus(false)
                .createdAt(ZonedDateTime.now())
                .build();
    }
}