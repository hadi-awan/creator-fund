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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    private static final AtomicInteger counter = new AtomicInteger(0);
    private Project testProject;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("user" + counter.incrementAndGet() + "@test.com");
        testUser.setFullName("Test User");
        testUser.setPasswordHash("hashedPassword");
        userRepository.save(testUser);

        // Create test category
        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category " + counter.get());
        categoryRepository.save(category);

        // Create test project
        testProject = new Project();
        testProject.setCreator(testUser);
        testProject.setCategory(category);
        testProject.setTitle("Test Project");
        testProject.setDescription("Test Description");
        testProject.setFundingGoal(new BigDecimal("1000.00"));
        testProject.setStartDate(ZonedDateTime.now());
        testProject.setEndDate(ZonedDateTime.now().plusDays(30));
        testProject.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(testProject);
    }

    private Comments createComment(String content, Comments parentComment) {
        Comments comment = new Comments();
        comment.setProject(testProject);
        comment.setUser(testUser);
        comment.setContent(content);
        comment.setParentComment(parentComment);
        comment.setStatus(CommentStatus.ACTIVE);
        return commentRepository.save(comment);
    }

    @Test
    void shouldFindCommentsByProject() {
        // Given
        createComment("First comment", null);
        createComment("Second comment", null);
        createComment("Third comment", null);

        // When
        Page<Comments> comments = commentRepository.findByProjectId(testProject.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(comments.getContent()).hasSize(3);
        assertThat(comments.getContent()).extracting("content")
                .containsExactlyInAnyOrder("First comment", "Second comment", "Third comment");
    }

    @Test
    void shouldFindRootComments() {
        // Given
        Comments rootComment1 = createComment("Root 1", null);
        Comments reply1 = createComment("Reply to 1", rootComment1);
        Comments rootComment2 = createComment("Root 2", null);
        Comments reply2 = createComment("Reply to 1 again", rootComment1);

        // When
        List<Comments> rootComments = commentRepository.findRootCommentsByProjectId(testProject.getId());

        // Then
        assertThat(rootComments).hasSize(2);
        assertThat(rootComments).extracting("content")
                .containsExactlyInAnyOrder("Root 1", "Root 2");
    }

    @Test
    void shouldFindReplies() {
        // Given
        Comments rootComment = createComment("Root comment", null);
        Comments reply1 = createComment("First reply", rootComment);
        Comments reply2 = createComment("Second reply", rootComment);

        // When
        List<Comments> replies = commentRepository.findByParentCommentId(rootComment.getId());

        // Then
        assertThat(replies).hasSize(2);
        assertThat(replies).extracting("content")
                .containsExactlyInAnyOrder("First reply", "Second reply");
    }
}