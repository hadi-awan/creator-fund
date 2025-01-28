package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        userRepository.save(user);

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        projectCategoryRepository.save(category);

        project = new Project();
        project.setCreator(user);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        projectRepository.save(project);
    }

    @Test
    void shouldFindCommentsByProject() {
        // Given
        Comments comment1 = createComment("First comment", null);
        Comments reply1 = createComment("Reply to first", comment1);
        Comments comment2 = createComment("Second comment", null);

        // When
        Page<Comments> comments = commentRepository.findByProjectId(project.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(comments.getContent()).hasSize(3);
        assertThat(comments.getContent()).extracting("content")
                .containsExactlyInAnyOrder("First comment", "Reply to first", "Second comment");
    }

    @Test
    void shouldFindRootComments() {
        // Given
        Comments rootComment1 = createComment("Root 1", null);
        Comments reply1 = createComment("Reply 1", rootComment1);
        Comments rootComment2 = createComment("Root 2", null);
        Comments reply2 = createComment("Reply 2", rootComment1);

        // When
        List<Comments> rootComments = commentRepository.findRootCommentsByProjectId(project.getId());

        // Then
        assertThat(rootComments).hasSize(2);
        assertThat(rootComments).extracting("content")
                .containsExactlyInAnyOrder("Root 1", "Root 2");
    }

    private Comments createComment(String content, Comments parentComment) {
        Comments comment = new Comments();
        comment.setProject(project);
        comment.setUser(user);
        comment.setContent(content);
        comment.setParentComment(parentComment);
        comment.setStatus(CommentStatus.ACTIVE);
        return commentRepository.save(comment);
    }
}
