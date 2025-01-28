package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjectUpdateRepositoryTest {

    @Autowired
    private ProjectUpdateRepository updateRepository;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectCategoryRepository categoryRepository;

    @Test
    void shouldFindUpdatesByProject() {
        // Given
        Project project = createTestProject();
        User creator = project.getCreator();

        ProjectUpdate update1 = new ProjectUpdate();
        update1.setProject(project);
        update1.setCreatedBy(creator);
        update1.setTitle("Update 1");
        update1.setContent("Content 1");
        update1.setUpdateType(UpdateType.GENERAL);
        updateRepository.save(update1);

        ProjectUpdate update2 = new ProjectUpdate();
        update2.setProject(project);
        update2.setCreatedBy(creator);
        update2.setTitle("Update 2");
        update2.setContent("Content 2");
        update2.setUpdateType(UpdateType.MILESTONE);
        updateRepository.save(update2);

        // When
        List<ProjectUpdate> updates = updateRepository.findByProjectIdOrderByCreatedAtDesc(project.getId());

        // Then
        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).getCreatedAt()).isAfterOrEqualTo(updates.get(1).getCreatedAt());
    }

    private Project createTestProject() {
        User creator = createTestUser("creator@test.com");

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        categoryRepository.save(category);

        Project project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        return projectRepository.save(project);
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }
}
