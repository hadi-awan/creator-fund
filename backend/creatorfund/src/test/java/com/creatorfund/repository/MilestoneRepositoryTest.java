package com.creatorfund.repository;


import com.creatorfund.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MilestoneRepositoryTest {
    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    @Test
    void shouldFindMilestonesByProject() {
        // Given
        Project project = createTestProject();

        Milestone milestone1 = createMilestone(project, "Milestone 1", LocalDate.now().plusDays(10));
        Milestone milestone2 = createMilestone(project, "Milestone 2", LocalDate.now().plusDays(20));

        // When
        List<Milestone> milestones = milestoneRepository.findByProjectId(project.getId());

        // Then
        assertThat(milestones).hasSize(2);
        assertThat(milestones)
                .extracting(Milestone::getTitle)
                .containsExactlyInAnyOrder("Milestone 1", "Milestone 2");
    }

    @Test
    void shouldFindMilestonesByStatus() {
        // Given
        Project project = createTestProject();

        Milestone pendingMilestone = createMilestone(project, "Pending Milestone", LocalDate.now().plusDays(10));
        pendingMilestone.setStatus(MilestoneStatus.PENDING);
        milestoneRepository.save(pendingMilestone);

        Milestone completedMilestone = createMilestone(project, "Completed Milestone", LocalDate.now().minusDays(5));
        completedMilestone.setStatus(MilestoneStatus.COMPLETED);
        milestoneRepository.save(completedMilestone);

        // When
        List<Milestone> pendingMilestones = milestoneRepository
                .findByProjectIdAndStatus(project.getId(), MilestoneStatus.PENDING);
        List<Milestone> completedMilestones = milestoneRepository
                .findByProjectIdAndStatus(project.getId(), MilestoneStatus.COMPLETED);

        // Then
        assertThat(pendingMilestones).hasSize(1);
        assertThat(completedMilestones).hasSize(1);
        assertThat(pendingMilestones.get(0).getTitle()).isEqualTo("Pending Milestone");
        assertThat(completedMilestones.get(0).getTitle()).isEqualTo("Completed Milestone");
    }

    @Test
    void shouldFindOverdueMilestones() {
        // Given
        Project project = createTestProject();

        Milestone overdueMilestone = createMilestone(project, "Overdue Milestone", LocalDate.now().minusDays(5));
        overdueMilestone.setStatus(MilestoneStatus.PENDING);
        milestoneRepository.save(overdueMilestone);

        Milestone futureMilestone = createMilestone(project, "Future Milestone", LocalDate.now().plusDays(5));
        futureMilestone.setStatus(MilestoneStatus.PENDING);
        milestoneRepository.save(futureMilestone);

        // When
        List<Milestone> overdueMilestones = milestoneRepository
                .findByTargetDateBeforeAndStatusNot(LocalDate.now(), MilestoneStatus.COMPLETED);

        // Then
        assertThat(overdueMilestones).hasSize(1);
        assertThat(overdueMilestones.get(0).getTitle()).isEqualTo("Overdue Milestone");
    }

    @Test
    void shouldFindUpcomingMilestones() {
        // Given
        Project project = createTestProject();
        LocalDate today = LocalDate.now();

        Milestone milestone1 = createMilestone(project, "Next Week", today.plusDays(7));
        Milestone milestone2 = createMilestone(project, "Next Month", today.plusDays(30));
        Milestone milestone3 = createMilestone(project, "Tomorrow", today.plusDays(1));

        // When
        List<Milestone> upcomingMilestones = milestoneRepository
                .findByTargetDateBetweenOrderByTargetDateAsc(today, today.plusDays(14));

        // Then
        assertThat(upcomingMilestones).hasSize(2);
        assertThat(upcomingMilestones)
                .extracting(Milestone::getTitle)
                .containsExactly("Tomorrow", "Next Week");
    }

    @Test
    void shouldOrderMilestonesByTargetDate() {
        // Given
        Project project = createTestProject();
        LocalDate today = LocalDate.now();

        createMilestone(project, "Last", today.plusDays(30));
        createMilestone(project, "First", today.plusDays(10));
        createMilestone(project, "Middle", today.plusDays(20));

        // When
        List<Milestone> orderedMilestones = milestoneRepository
                .findByProjectIdOrderByTargetDateAsc(project.getId());

        // Then
        assertThat(orderedMilestones).hasSize(3);
        assertThat(orderedMilestones)
                .extracting(Milestone::getTitle)
                .containsExactly("First", "Middle", "Last");
    }

    // Helper methods
    private Project createTestProject() {
        User creator = new User();
        creator.setEmail("creator@test.com");
        creator.setFullName("Test Creator");
        creator.setPasswordHash("hashedPassword");
        userRepository.save(creator);

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        projectCategoryRepository.save(category);

        Project project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(java.time.ZonedDateTime.now());
        project.setEndDate(java.time.ZonedDateTime.now().plusDays(30));
        return projectRepository.save(project);
    }

    private Milestone createMilestone(Project project, String title, LocalDate targetDate) {
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(title);
        milestone.setDescription("Description for " + title);
        milestone.setTargetDate(targetDate);
        milestone.setStatus(MilestoneStatus.PENDING);
        milestone.setFundReleaseAmount(new BigDecimal("100.00"));
        return milestoneRepository.save(milestone);
    }
}