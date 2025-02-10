package com.creatorfund.repository;

import com.creatorfund.model.*;
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
public class ProjectRepositoryTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    private static final AtomicInteger counter = new AtomicInteger(0);

    private User createUser() {
        String uniqueEmail = "creator" + counter.incrementAndGet() + "@test.com";
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setFullName("Test Creator " + counter.get());
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }

    private ProjectCategory createCategory() {
        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category " + counter.get());
        return categoryRepository.save(category);
    }

    private Project createTestProject(String title, ProjectStatus status) {
        User creator = createUser();
        ProjectCategory category = createCategory();

        Project project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle(title);
        project.setDescription("Description for " + title);
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        project.setStatus(status);
        project.setCurrentAmount(BigDecimal.ZERO);
        return projectRepository.save(project);
    }

    @Test
    void shouldFindProjectsByStatus() {
        // Given
        createTestProject("Active Project", ProjectStatus.ACTIVE);
        createTestProject("Draft Project", ProjectStatus.DRAFT);
        createTestProject("Another Active", ProjectStatus.ACTIVE);

        // When
        List<Project> activeProjects = projectRepository.findByStatus(ProjectStatus.ACTIVE);
        List<Project> draftProjects = projectRepository.findByStatus(ProjectStatus.DRAFT);

        // Then
        assertThat(activeProjects).hasSize(2);
        assertThat(draftProjects).hasSize(1);
        assertThat(activeProjects).extracting("title")
                .containsExactlyInAnyOrder("Active Project", "Another Active");
    }

    @Test
    void shouldFindByTitleContaining() {
        // Given
        createTestProject("Creative Writing", ProjectStatus.ACTIVE);
        createTestProject("Writing Workshop", ProjectStatus.ACTIVE);
        createTestProject("Art Project", ProjectStatus.ACTIVE);

        // When
        Page<Project> writingProjects = projectRepository.findByTitleContainingIgnoreCase("writing",
                PageRequest.of(0, 10));

        // Then
        assertThat(writingProjects.getContent()).hasSize(2);
        assertThat(writingProjects.getContent()).extracting("title")
                .containsExactlyInAnyOrder("Creative Writing", "Writing Workshop");
    }

    @Test
    void shouldFindFullyFundedActiveProjects() {
        // Given
        Project fullyFunded = createTestProject("Fully Funded", ProjectStatus.ACTIVE);
        fullyFunded.setFundingGoal(new BigDecimal("1000.00"));
        fullyFunded.setCurrentAmount(new BigDecimal("1200.00"));
        projectRepository.save(fullyFunded);

        Project partiallyFunded = createTestProject("Partially Funded", ProjectStatus.ACTIVE);
        partiallyFunded.setFundingGoal(new BigDecimal("1000.00"));
        partiallyFunded.setCurrentAmount(new BigDecimal("500.00"));
        projectRepository.save(partiallyFunded);

        // When
        List<Project> fullyFundedProjects = projectRepository.findFullyFundedActiveProjects();

        // Then
        assertThat(fullyFundedProjects).hasSize(1);
        assertThat(fullyFundedProjects.get(0).getTitle()).isEqualTo("Fully Funded");
    }
}