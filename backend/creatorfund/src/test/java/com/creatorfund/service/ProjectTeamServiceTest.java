package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateProjectTeamRequest;
import com.creatorfund.dto.request.UpdateProjectTeamRequest;
import com.creatorfund.dto.response.ProjectTeamMemberResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.mapper.ProjectTeamMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.ProjectTeamRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectTeamServiceTest extends BaseServiceTest {

    @Mock
    private ProjectTeamRepository projectTeamRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectTeamMapper projectTeamMapper;

    @Mock
    private NotificationService notificationService;

    private ProjectTeamService projectTeamService;

    @BeforeEach
    void setUp() {
        projectTeamService = new ProjectTeamService(
                projectTeamRepository,
                projectRepository,
                userRepository,
                projectTeamMapper,
                notificationService
        );
    }

    @Test
    void addTeamMember_Success() {
        // Arrange
        UUID addedBy = UUID.randomUUID();
        CreateProjectTeamRequest request = createSampleTeamRequest();
        User user = createSampleUser();
        User creator = createSampleUser();
        creator.setId(addedBy);  // Set creator ID to match addedBy
        Project project = createSampleProject(creator);
        ProjectTeam teamMember = createSampleTeamMember(user, project);
        ProjectTeamMemberResponse expectedResponse = createSampleTeamResponse();

        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(projectTeamRepository.existsByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(false);
        when(projectTeamMapper.toEntity(request)).thenReturn(teamMember);
        when(projectTeamRepository.save(any(ProjectTeam.class))).thenReturn(teamMember);
        when(projectTeamMapper.toResponse(teamMember)).thenReturn(expectedResponse);

        // Act
        ProjectTeamMemberResponse result = projectTeamService.addTeamMember(request, addedBy);

        // Assert
        assertThat(result).isNotNull();
        verify(projectTeamRepository).save(any(ProjectTeam.class));
    }

    @Test
    void addTeamMember_UserAlreadyMember() {
        // Arrange
        UUID addedBy = UUID.randomUUID();
        CreateProjectTeamRequest request = createSampleTeamRequest();
        User user = createSampleUser();
        User creator = createSampleUser();
        creator.setId(addedBy);  // Set creator ID to match addedBy
        Project project = createSampleProject(creator);

        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(projectTeamRepository.existsByProjectIdAndUserId(project.getId(), user.getId()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> projectTeamService.addTeamMember(request, addedBy))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("User is already a team member");
    }

    @Test
    void updateTeamMember_Success() {
        // Arrange
        UUID teamMemberId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UpdateProjectTeamRequest request = createSampleUpdateRequest();
        User creator = createSampleUser();
        creator.setId(updatedBy);  // Set creator ID to match updatedBy
        Project project = createSampleProject(creator);
        ProjectTeam teamMember = createSampleTeamMember(createSampleUser(), project);
        ProjectTeamMemberResponse expectedResponse = createSampleTeamResponse();

        when(projectTeamRepository.findById(teamMemberId)).thenReturn(Optional.of(teamMember));
        when(projectTeamRepository.save(teamMember)).thenReturn(teamMember);
        when(projectTeamMapper.toResponse(teamMember)).thenReturn(expectedResponse);

        // Act
        ProjectTeamMemberResponse result = projectTeamService.updateTeamMember(teamMemberId, request, updatedBy);

        // Assert
        assertThat(result).isNotNull();
        verify(projectTeamRepository).save(teamMember);
    }

    @Test
    void removeTeamMember_Success() {
        // Arrange
        UUID teamMemberId = UUID.randomUUID();
        UUID removedBy = UUID.randomUUID();
        User creator = createSampleUser();
        creator.setId(removedBy);  // Set creator ID to match removedBy
        Project project = createSampleProject(creator);
        ProjectTeam teamMember = createSampleTeamMember(createSampleUser(), project);

        when(projectTeamRepository.findById(teamMemberId)).thenReturn(Optional.of(teamMember));

        // Act
        projectTeamService.removeTeamMember(teamMemberId, removedBy);

        // Assert
        verify(projectTeamRepository).delete(teamMember);
        verify(notificationService).createNotification(any(), any(), any(), any());
    }

    @Test
    void getProjectTeam_Success() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        List<ProjectTeam> teamMembers = Arrays.asList(
                createSampleTeamMember(createSampleUser(), createSampleProject(createSampleUser())),
                createSampleTeamMember(createSampleUser(), createSampleProject(createSampleUser()))
        );
        List<ProjectTeamMemberResponse> expectedResponses = Arrays.asList(
                createSampleTeamResponse(),
                createSampleTeamResponse()
        );

        when(projectTeamRepository.findByProjectId(projectId)).thenReturn(teamMembers);
        when(projectTeamMapper.toResponse(any(ProjectTeam.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        // Act
        List<ProjectTeamMemberResponse> result = projectTeamService.getProjectTeam(projectId);

        // Assert
        assertThat(result).hasSize(2);
    }

    // Helper methods
    private CreateProjectTeamRequest createSampleTeamRequest() {
        CreateProjectTeamRequest request = new CreateProjectTeamRequest();
        request.setProjectId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
        request.setRole(TeamRole.CONTRIBUTOR.name());
        return request;
    }

    private UpdateProjectTeamRequest createSampleUpdateRequest() {
        UpdateProjectTeamRequest request = new UpdateProjectTeamRequest();
        request.setRole(TeamRole.MODERATOR.name());
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

    private ProjectTeam createSampleTeamMember(User user, Project project) {
        ProjectTeam teamMember = new ProjectTeam();
        teamMember.setId(UUID.randomUUID());
        teamMember.setUser(user);
        teamMember.setProject(project);
        teamMember.setRole(TeamRole.CONTRIBUTOR);
        teamMember.setJoinedAt(ZonedDateTime.now());
        return teamMember;
    }

    private ProjectTeamMemberResponse createSampleTeamResponse() {
        return ProjectTeamMemberResponse.builder()
                .id(UUID.randomUUID())
                .role(TeamRole.CONTRIBUTOR.name())
                .joinedAt(ZonedDateTime.now())
                .build();
    }
}