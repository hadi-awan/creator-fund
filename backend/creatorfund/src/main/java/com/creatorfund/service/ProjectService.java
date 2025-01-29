package com.creatorfund.service;

import com.creatorfund.dto.request.CreateProjectRequest;
import com.creatorfund.dto.request.ProjectSearchRequest;
import com.creatorfund.dto.request.UpdateProjectRequest;
import com.creatorfund.dto.response.ProjectDetailsResponse;
import com.creatorfund.dto.response.ProjectSummaryResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.ProjectMapper;
import com.creatorfund.model.Project;
import com.creatorfund.model.ProjectCategory;
import com.creatorfund.model.User;
import com.creatorfund.repository.ProjectCategoryRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final ProjectMapper projectMapper;

    public ProjectDetailsResponse createProject(CreateProjectRequest request, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectCategory category = projectCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Project project = projectMapper.toEntity(request);
        project.setCreator(creator);
        project.setCategory(category);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toDetailsResponse(savedProject);
    }

    public ProjectDetailsResponse getProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return projectMapper.toDetailsResponse(project);
    }

    public Page<ProjectSummaryResponse> searchProjects(ProjectSearchRequest request, Pageable pageable) {
        // Implement search logic using criteria or specifications
        return projectRepository.findAll(pageable)
                .map(projectMapper::toSummaryResponse);
    }

    public ProjectDetailsResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectMapper.updateEntity(project, request);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toDetailsResponse(updatedProject);
    }
}
