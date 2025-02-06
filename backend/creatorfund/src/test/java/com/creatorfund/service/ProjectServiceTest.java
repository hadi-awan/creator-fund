package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;

import com.creatorfund.dto.request.CreateProjectRequest;
import com.creatorfund.dto.response.ProjectDetailsResponse;
import com.creatorfund.dto.response.ProjectSummaryResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.ProjectMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.ProjectCategoryRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectServiceTest extends BaseServiceTest{
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectCategoryRepository categoryRepository;

    @Mock
    private ProjectMapper projectMapper;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(
                projectRepository,
                userRepository,
                categoryRepository,
                projectMapper
        );
    }

    @Test
    void createProject_Success() {
        // Arrange
        UUID creatorId = UUID.randomUUID();
        CreateProjectRequest request = createSampleProjectRequest();
        User creator = createSampleUser(creatorId);
        ProjectCategory category = createSampleCategory();
        Project project = createSampleProject(creator, category);
        ProjectDetailsResponse expectedResponse = createSampleProjectResponse();

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(projectMapper.toEntity(request)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toDetailsResponse(project)).thenReturn(expectedResponse);

        // Act
        ProjectDetailsResponse result = projectService.createProject(request, creatorId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedResponse.getId());
        assertThat(result.getTitle()).isEqualTo(expectedResponse.getTitle());

        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toDetailsResponse(any(Project.class));
    }

    @Test
    void createProject_UserNotFound() {
        // Arrange
        UUID creatorId = UUID.randomUUID();
        CreateProjectRequest request = createSampleProjectRequest();

        when(userRepository.findById(creatorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.createProject(request, creatorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    void getProject_Success() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject(createSampleUser(UUID.randomUUID()), createSampleCategory());
        ProjectDetailsResponse expectedResponse = createSampleProjectResponse();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toDetailsResponse(project)).thenReturn(expectedResponse);

        // Act
        ProjectDetailsResponse result = projectService.getProject(projectId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedResponse.getId());
        verify(projectMapper).toDetailsResponse(project);
    }

    @Test
    void getProject_NotFound() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void searchProjects_Success() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Project project = createSampleProject(createSampleUser(UUID.randomUUID()), createSampleCategory());
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project));
        ProjectSummaryResponse summaryResponse = createSampleProjectSummaryResponse();

        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
        when(projectMapper.toSummaryResponse(project)).thenReturn(summaryResponse);

        // Act
        Page<ProjectSummaryResponse> result = projectService.searchProjects(null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(summaryResponse.getTitle());
    }

    // Helper methods to create test data
    private CreateProjectRequest createSampleProjectRequest() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitle("Test Project");
        request.setDescription("Test Description");
        request.setFundingGoal(BigDecimal.valueOf(1000));
        request.setCategoryId(UUID.randomUUID());
        request.setStartDate(ZonedDateTime.now());
        request.setEndDate(ZonedDateTime.now().plusDays(30));
        return request;
    }

    private User createSampleUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        return user;
    }

    private ProjectCategory createSampleCategory() {
        ProjectCategory category = new ProjectCategory();
        category.setId(UUID.randomUUID());
        category.setName("Test Category");
        return category;
    }

    private Project createSampleProject(User creator, ProjectCategory category) {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setCreator(creator);
        project.setCategory(category);
        project.setFundingGoal(BigDecimal.valueOf(1000));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        return project;
    }

    private ProjectDetailsResponse createSampleProjectResponse() {
        return ProjectDetailsResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .description("Test Description")
                .fundingGoal(BigDecimal.valueOf(1000))
                .status("DRAFT")
                .build();
    }

    private ProjectSummaryResponse createSampleProjectSummaryResponse() {
        return ProjectSummaryResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .shortDescription("Test Description")
                .fundingGoal(BigDecimal.valueOf(1000))
                .status("DRAFT")
                .build();
    }
}
