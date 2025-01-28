package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjectTeamRepositoryTest {
    @Autowired
    private ProjectTeamRepository projectTeamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    private Project project;
    private User projectCreator;

    @BeforeEach
    void setUp() {
        projectCreator = new User();
        projectCreator.setEmail("creator@test.com");
        projectCreator.setFullName("Project Creator");
        projectCreator.setPasswordHash("hashedPassword");
        userRepository.save(projectCreator);

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        projectCategoryRepository.save(category);

        project = new Project();
        project.setCreator(projectCreator);
        project.setCategory(category);
        project.setTitle("Team Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        projectRepository.save(project);
    }

    @Test
    void shouldFindTeamMembersByProject() {
        // Given
        User member1 = createUser("member1@test.com", "Team Member 1");
        User member2 = createUser("member2@test.com", "Team Member 2");

        createProjectTeam(member1, TeamRole.CONTRIBUTOR);
        createProjectTeam(member2, TeamRole.MODERATOR);

        // When
        List<ProjectTeam> teamMembers = projectTeamRepository.findByProjectId(project.getId());

        // Then
        assertThat(teamMembers).hasSize(2);
        assertThat(teamMembers).extracting("user.email")
                .containsExactlyInAnyOrder("member1@test.com", "member2@test.com");
    }

    @Test
    void shouldCheckTeamMembership() {
        // Given
        User member = createUser("member@test.com", "Team Member");
        createProjectTeam(member, TeamRole.CONTRIBUTOR);

        // When & Then
        assertThat(projectTeamRepository.existsByProjectIdAndUserId(project.getId(), member.getId())).isTrue();
        assertThat(projectTeamRepository.existsByProjectIdAndUserId(project.getId(), projectCreator.getId())).isFalse();
    }

    private User createUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }

    private void createProjectTeam(User user, TeamRole role) {
        ProjectTeam teamMember = new ProjectTeam();
        teamMember.setProject(project);
        teamMember.setUser(user);
        teamMember.setRole(role);
        teamMember.setPermissions("{}"); // Empty JSON object for default permissions
        projectTeamRepository.save(teamMember);
    }
}
