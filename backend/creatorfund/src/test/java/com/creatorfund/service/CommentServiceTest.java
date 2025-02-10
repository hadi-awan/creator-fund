package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateCommentRequest;
import com.creatorfund.dto.response.CommentResponse;
import com.creatorfund.mapper.CommentMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.CommentRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentServiceTest extends BaseServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NotificationService notificationService;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(
                commentRepository,
                projectRepository,
                userRepository,
                commentMapper,
                notificationService
        );
    }

    @Test
    void createComment_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateCommentRequest request = createSampleCommentRequest();
        User user = createSampleUser();
        User projectCreator = createSampleUser();
        Project project = createSampleProject(projectCreator);
        Comments comment = createSampleComment(user, project);
        CommentResponse expectedResponse = createSampleCommentResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(commentMapper.toEntity(request)).thenReturn(comment);
        when(commentRepository.save(any(Comments.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponse);
        doNothing().when(notificationService).createNotification(any(), any(), any(), any());

        // Act
        CommentResponse result = commentService.createComment(request, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(expectedResponse.getContent());
        verify(commentRepository).save(any(Comments.class));
        verify(notificationService).createNotification(any(), any(), any(), any());
    }

    @Test
    void createComment_WithParentComment_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateCommentRequest request = createSampleCommentRequestWithParent();
        User user = createSampleUser();
        User projectCreator = createSampleUser();
        Project project = createSampleProject(projectCreator);

        // Create and set up parent comment with proper status
        Comments parentComment = createSampleComment(user, project);
        parentComment.setId(request.getParentCommentId());
        parentComment.setProject(project);
        parentComment.setStatus(CommentStatus.ACTIVE);  // Make sure we use the enum value directly

        Comments reply = createSampleComment(user, project);
        reply.setParentComment(parentComment);
        CommentResponse expectedResponse = createSampleCommentResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(commentRepository.findById(request.getParentCommentId())).thenReturn(Optional.of(parentComment));
        when(commentMapper.toEntity(request)).thenReturn(reply);
        when(commentRepository.save(any(Comments.class))).thenReturn(reply);
        when(commentMapper.toResponse(reply)).thenReturn(expectedResponse);

        // Act
        CommentResponse result = commentService.createComment(request, userId);

        // Assert
        assertThat(result).isNotNull();
        verify(commentRepository).save(any(Comments.class));
    }

    @Test
    void updateCommentStatus_Success() {
        // Arrange
        UUID commentId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        String newStatus = "HIDDEN";

        // Create moderator user
        User moderator = createSampleUser();
        moderator.setId(moderatorId);

        // Create project creator (set as moderator for this test)
        Project project = createSampleProject(moderator); // Set moderator as project creator

        // Create the comment to be moderated
        Comments comment = createSampleComment(createSampleUser(), project);

        // Set up project team with moderator
        ProjectTeam moderatorTeam = new ProjectTeam();
        moderatorTeam.setUser(moderator);
        moderatorTeam.setRole(TeamRole.MODERATOR);
        project.getProjectTeam().add(moderatorTeam);

        CommentResponse expectedResponse = createSampleCommentResponse();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponse);

        // Act
        CommentResponse result = commentService.updateCommentStatus(commentId, newStatus, moderatorId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(expectedResponse.getStatus());
        verify(commentRepository).save(comment);
    }

    // Helper methods
    private CreateCommentRequest createSampleCommentRequest() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Test comment");
        request.setProjectId(UUID.randomUUID());
        return request;
    }

    private CreateCommentRequest createSampleCommentRequestWithParent() {
        CreateCommentRequest request = createSampleCommentRequest();
        request.setParentCommentId(UUID.randomUUID());
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

    private Comments createSampleComment(User user, Project project) {
        Comments comment = new Comments();
        comment.setId(UUID.randomUUID());
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setProject(project);
        comment.setCreatedAt(ZonedDateTime.now());
        comment.setStatus(CommentStatus.valueOf("ACTIVE"));  // Explicitly set status
        return comment;
    }

    private CommentResponse createSampleCommentResponse() {
        return CommentResponse.builder()
                .id(UUID.randomUUID())
                .content("Test comment")
                .status("ACTIVE")
                .createdAt(ZonedDateTime.now())
                .build();
    }
}