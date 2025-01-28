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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectTeamRepositoryTest {
    @Autowired
    private ProjectTeamRepository projectTeamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    private static final AtomicInteger counter = new AtomicInteger(0);
    private Project testProject;
    private User projectOwner;

    @BeforeEach
    void setUp() {
        // Create project owner
        projectOwner = new User();
        projectOwner.setEmail("owner" + counter.incrementAndGet() + "@test.com");
        projectOwner.setFullName("Project Owner");
        projectOwner.setPasswordHash("hashedPassword");
        userRepository.save(projectOwner);

        // Create category
        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category " + counter.get());
        categoryRepository.save(category);

        // Create project
        testProject = new Project();
        testProject.setCreator(projectOwner);
        testProject.setCategory(category);
        testProject.setTitle("Test Project");
        testProject.setDescription("Test Description");
        testProject.setFundingGoal(new BigDecimal("1000.00"));
        testProject.setStartDate(ZonedDateTime.now());
        testProject.setEndDate(ZonedDateTime.now().plusDays(30));
        testProject.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(testProject);
    }

    private User createTeamMember(String suffix) {
        User member = new User();
        member.setEmail("member" + counter.incrementAndGet() + suffix + "@test.com");
        member.setFullName("Team Member " + suffix);
        member.setPasswordHash("hashedPassword");
        return userRepository.save(member);
    }

    private ProjectTeam addTeamMember(User user, TeamRole role) {
        ProjectTeam teamMember = new ProjectTeam();
        teamMember.setProject(testProject);
        teamMember.setUser(user);
        teamMember.setRole(role);
        teamMember.setPermissions("{}");
        return projectTeamRepository.save(teamMember);
    }

    @Test
    void shouldFindTeamMembersByProject() {
        // Given
        User member1 = createTeamMember("One");
        User member2 = createTeamMember("Two");
        addTeamMember(member1, TeamRole.CONTRIBUTOR);
        addTeamMember(member2, TeamRole.MODERATOR);

        // When
        List<ProjectTeam> teamMembers = projectTeamRepository.findByProjectId(testProject.getId());

        // Then
        assertThat(teamMembers).hasSize(2);
        assertThat(teamMembers).extracting("user.email")
                .containsExactlyInAnyOrder(member1.getEmail(), member2.getEmail());
    }

    @Test
    void shouldCheckTeamMembership() {
        // Given
        User member = createTeamMember("Check");
        addTeamMember(member, TeamRole.CONTRIBUTOR);

        // When & Then
        assertThat(projectTeamRepository.existsByProjectIdAndUserId(testProject.getId(), member.getId()))
                .isTrue();
        assertThat(projectTeamRepository.existsByProjectIdAndUserId(testProject.getId(), projectOwner.getId()))
                .isFalse();
    }

    @Test
    void shouldFindByUserId() {
        // Given
        User member = createTeamMember("Multi");
        Project anotherProject = new Project();
        anotherProject.setCreator(projectOwner);
        anotherProject.setCategory(testProject.getCategory());
        anotherProject.setTitle("Another Project");
        anotherProject.setDescription("Another Description");
        anotherProject.setFundingGoal(new BigDecimal("2000.00"));
        anotherProject.setStartDate(ZonedDateTime.now());
        anotherProject.setEndDate(ZonedDateTime.now().plusDays(30));
        anotherProject.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(anotherProject);

        addTeamMember(member, TeamRole.CONTRIBUTOR);
        ProjectTeam anotherTeam = new ProjectTeam();
        anotherTeam.setProject(anotherProject);
        anotherTeam.setUser(member);
        anotherTeam.setRole(TeamRole.MODERATOR);
        anotherTeam.setPermissions("{}");
        projectTeamRepository.save(anotherTeam);

        // When
        List<ProjectTeam> memberTeams = projectTeamRepository.findByUserId(member.getId());

        // Then
        assertThat(memberTeams).hasSize(2);
        assertThat(memberTeams).extracting("project.title")
                .containsExactlyInAnyOrder("Test Project", "Another Project");
    }
}