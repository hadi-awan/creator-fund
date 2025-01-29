package com.creatorfund.service;

import com.creatorfund.dto.request.CreateCommentRequest;
import com.creatorfund.dto.response.CommentResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.CommentMapper;
import com.creatorfund.model.CommentStatus;
import com.creatorfund.model.Comments;
import com.creatorfund.model.Project;
import com.creatorfund.model.User;
import com.creatorfund.repository.CommentRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    public CommentResponse createComment(CreateCommentRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Comments parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            validateParentComment(parentComment, project.getId());
        }

        Comments comment = commentMapper.toEntity(request);
        comment.setUser(user);
        comment.setProject(project);
        comment.setParentComment(parentComment);

        Comments savedComment = commentRepository.save(comment);

        // Notify project creator about new comment
        notifyNewComment(savedComment);

        // If this is a reply, notify parent comment author
        if (parentComment != null) {
            notifyCommentReply(savedComment);
        }

        return commentMapper.toResponse(savedComment);
    }

    public Page<CommentResponse> getProjectComments(UUID projectId, Pageable pageable) {
        return commentRepository.findByProjectId(projectId, pageable)
                .map(commentMapper::toResponse);
    }

    public List<CommentResponse> getRootComments(UUID projectId) {
        return commentRepository.findRootCommentsByProjectId(projectId)
                .stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentReplies(UUID commentId) {
        return commentRepository.findByParentCommentId(commentId)
                .stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse updateCommentStatus(UUID commentId, String status, UUID moderatorId) {
        Comments comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        validateModerator(moderatorId, comment.getProject());

        comment.setStatus(CommentStatus.valueOf(status));
        Comments updatedComment = commentRepository.save(comment);
        return commentMapper.toResponse(updatedComment);
    }

    private void validateParentComment(Comments parentComment, UUID projectId) {
        if (!parentComment.getProject().getId().equals(projectId)) {
            throw new BusinessValidationException("Parent comment must belong to the same project");
        }
        throw new BusinessValidationException("Cannot reply to an inactive comment");
    }

    private void validateModerator(UUID moderatorId, Project project) {
        // Check if the user is either project creator or team member with moderation rights
        boolean isModerator = project.getCreator().getId().equals(moderatorId);

        if (!isModerator) {
            throw new BusinessValidationException("User is not authorized to moderate comments");
        }
    }

    private void notifyNewComment(Comments comment) {
        Project project = comment.getProject();
        notificationService.createNotification(
                project.getCreator().getId(),
                String.format("New comment on your project '%s'", project.getTitle()),
                "NEW_COMMENT",
                comment.getId()
        );
    }

    private void notifyCommentReply(Comments reply) {
        Comments parentComment = reply.getParentComment();
        notificationService.createNotification(
                parentComment.getUser().getId(),
                String.format("New reply to your comment on '%s'", reply.getProject().getTitle()),
                "COMMENT_REPLY",
                reply.getId()
        );
    }
}
