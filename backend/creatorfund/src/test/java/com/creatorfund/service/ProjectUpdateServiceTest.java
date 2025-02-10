package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateProjectUpdateRequest;
import com.creatorfund.dto.response.ProjectUpdateResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.mapper.ProjectUpdateMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.ProjectUpdateRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectUpdateServiceTest extends BaseServiceTest {

    @Mock
    private ProjectUpdateRepository updateRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectUpdateMapper updateMapper;

    @Mock
    private NotificationService notificationService;

    private ProjectUpdateService updateService;

    @BeforeEach
    void setUp() {
        updateService = new ProjectUpdateService(
                updateRepository,
                projectRepository,
                userRepository,
                updateMapper,
                notificationService
        );
    }

    @Test
    void createUpdate_Success() {
        UUID projectId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        CreateProjectUpdateRequest request = createSampleUpdateRequest();
        User creator = createSampleUser();
        creator.setId(creatorId);
        Project project = createSampleProject(creator);
        ProjectUpdate update = createSampleUpdate(creator, project);
        ProjectUpdateResponse expectedResponse = createSampleUpdateResponse();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(updateMapper.toEntity(request)).thenReturn(update);
        when(updateRepository.save(any(ProjectUpdate.class))).thenReturn(update);
        when(updateMapper.toResponse(update)).thenReturn(expectedResponse);

        ProjectUpdateResponse result = updateService.createUpdate(request, projectId, creatorId);

        assertThat(result).isNotNull();
        verify(updateRepository).save(any(ProjectUpdate.class));
    }

    @Test
    void createUpdate_NotAuthorized() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        CreateProjectUpdateRequest request = createSampleUpdateRequest();
        User creator = createSampleUser();
        User projectCreator = createSampleUser();
        Project project = createSampleProject(projectCreator);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));

        // Act & Assert
        assertThatThrownBy(() -> updateService.createUpdate(request, projectId, creatorId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Not authorized to create project updates");
    }

    @Test
    void getProjectUpdates_Success() {
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject(createSampleUser());
        ProjectUpdate update = createSampleUpdate(createSampleUser(), project);
        List<ProjectUpdate> updates = List.of(update);

        when(updateRepository.findByProjectIdOrderByCreatedAtDesc(projectId))
                .thenReturn(updates);

        List<ProjectUpdate> result = updateService.getProjectUpdates(projectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(update.getTitle());
    }

    @Test
    void getUpdatesByType_Success() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject(createSampleUser());
        ProjectUpdate update = createSampleUpdate(createSampleUser(), project);
        ProjectUpdateResponse expectedResponse = createSampleUpdateResponse();

        when(updateRepository.findByProjectIdAndUpdateType(projectId, UpdateType.MILESTONE))
                .thenReturn(List.of(update));
        when(updateMapper.toResponse(update)).thenReturn(expectedResponse);

        // Act
        List<ProjectUpdateResponse> result = updateService.getUpdatesByType(projectId, UpdateType.MILESTONE);

        // Assert
        assertThat(result).hasSize(1);
    }

    // Helper methods
    private CreateProjectUpdateRequest createSampleUpdateRequest() {
        CreateProjectUpdateRequest request = new CreateProjectUpdateRequest();
        request.setTitle("Test Update");
        request.setContent("Test Content");
        request.setUpdateType(UpdateType.MILESTONE.name());
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

    private ProjectUpdate createSampleUpdate(User creator, Project project) {
        ProjectUpdate update = new ProjectUpdate();
        update.setId(UUID.randomUUID());
        update.setTitle("Test Update");
        update.setContent("Test Content");
        update.setCreatedBy(creator);
        update.setProject(project);
        update.setUpdateType(UpdateType.MILESTONE);
        update.setCreatedAt(ZonedDateTime.now());
        return update;
    }

    private ProjectUpdateResponse createSampleUpdateResponse() {
        return ProjectUpdateResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Update")
                .content("Test Content")
                .updateType(UpdateType.MILESTONE.name())
                .createdAt(ZonedDateTime.now())
                .build();
    }
}