package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateMilestoneRequest;
import com.creatorfund.dto.response.MilestoneResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.MilestoneMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.MilestoneRepository;
import com.creatorfund.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
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

class MilestoneServiceTest extends BaseServiceTest {

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MilestoneMapper milestoneMapper;

    @Mock
    private NotificationService notificationService;

    private MilestoneService milestoneService;

    @BeforeEach
    void setUp() {
        milestoneService = new MilestoneService(
                milestoneRepository,
                projectRepository,
                milestoneMapper,
                notificationService
        );
    }

    @Test
    void createMilestone_Success() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject();
        CreateMilestoneRequest request = createSampleMilestoneRequest(project.getEndDate().toLocalDate());
        Milestone milestone = createSampleMilestone(project);
        MilestoneResponse expectedResponse = createSampleMilestoneResponse(project.getEndDate().toLocalDate());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(milestoneMapper.toEntity(request)).thenReturn(milestone);
        when(milestoneRepository.save(any(Milestone.class))).thenReturn(milestone);
        when(milestoneMapper.toResponse(milestone)).thenReturn(expectedResponse);

        // Act
        MilestoneResponse result = milestoneService.createMilestone(projectId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(expectedResponse.getTitle());
        verify(milestoneRepository).save(any(Milestone.class));
    }

    @Test
    void createMilestone_ProjectNotFound() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        CreateMilestoneRequest request = createSampleMilestoneRequest(LocalDate.now().plusDays(20));

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> milestoneService.createMilestone(projectId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void completeMilestone_Success() {
        // Arrange
        UUID milestoneId = UUID.randomUUID();
        Project project = createSampleProject();
        Milestone milestone = createSampleMilestone(project);
        milestone.setStatus(MilestoneStatus.PENDING);
        MilestoneResponse expectedResponse = createSampleMilestoneResponse(project.getEndDate().toLocalDate());

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(milestoneRepository.save(milestone)).thenReturn(milestone);
        when(milestoneMapper.toResponse(milestone)).thenReturn(expectedResponse);

        // Act
        MilestoneResponse result = milestoneService.completeMilestone(milestoneId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.COMPLETED);
        assertThat(milestone.getCompletionDate()).isNotNull();
        verify(notificationService).createNotification(any(), any(), any(), any());
    }

    @Test
    void completeMilestone_AlreadyCompleted() {
        // Arrange
        UUID milestoneId = UUID.randomUUID();
        Project project = createSampleProject();
        Milestone milestone = createSampleMilestone(project);
        milestone.setStatus(MilestoneStatus.COMPLETED);

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));

        // Act & Assert
        assertThatThrownBy(() -> milestoneService.completeMilestone(milestoneId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Milestone is already completed");
    }

    @Test
    void getProjectMilestones_Success() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject();
        List<Milestone> milestones = Arrays.asList(
                createSampleMilestone(project),
                createSampleMilestone(project)
        );
        List<MilestoneResponse> expectedResponses = Arrays.asList(
                createSampleMilestoneResponse(project.getEndDate().toLocalDate()),
                createSampleMilestoneResponse(project.getEndDate().toLocalDate())
        );

        when(milestoneRepository.findByProjectIdOrderByTargetDateAsc(projectId))
                .thenReturn(milestones);
        when(milestoneMapper.toResponse(any(Milestone.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        // Act
        List<MilestoneResponse> result = milestoneService.getProjectMilestones(projectId);

        // Assert
        assertThat(result).hasSize(2);
        verify(milestoneRepository).findByProjectIdOrderByTargetDateAsc(projectId);
    }

    // Helper methods
    private CreateMilestoneRequest createSampleMilestoneRequest(LocalDate projectEndDate) {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setTitle("Test Milestone");
        request.setDescription("Test Description");
        request.setTargetDate(projectEndDate.minusDays(5));
        request.setFundReleaseAmount(new BigDecimal("1000.00"));
        return request;
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        return user;
    }

    private Project createSampleProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test Project");
        project.setCreator(createSampleUser());

        ZonedDateTime now = ZonedDateTime.now();
        project.setStartDate(now);
        project.setEndDate(now.plusMonths(1));
        project.setProjectTeam(new ArrayList<>());
        return project;
    }

    private Milestone createSampleMilestone(Project project) {
        Milestone milestone = new Milestone();
        milestone.setId(UUID.randomUUID());
        milestone.setTitle("Test Milestone");
        milestone.setDescription("Test Description");
        milestone.setProject(project);
        milestone.setTargetDate(project.getEndDate().toLocalDate().minusDays(5));
        milestone.setFundReleaseAmount(new BigDecimal("1000.00"));
        milestone.setStatus(MilestoneStatus.PENDING);
        return milestone;
    }

    private MilestoneResponse createSampleMilestoneResponse(LocalDate projectEndDate) {
        return MilestoneResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Milestone")
                .description("Test Description")
                .targetDate(projectEndDate.minusDays(5))
                .status(MilestoneStatus.PENDING.name())
                .fundReleaseAmount(new BigDecimal("1000.00"))
                .build();
    }
}