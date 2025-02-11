package com.creatorfund.service;

import com.creatorfund.dto.request.CreateProjectUpdateRequest;
import com.creatorfund.dto.response.ProjectUpdateResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.ProjectUpdateMapper;
import com.creatorfund.model.Project;
import com.creatorfund.model.ProjectUpdate;
import com.creatorfund.model.UpdateType;
import com.creatorfund.model.User;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.ProjectUpdateRepository;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectUpdateService {
    private final ProjectUpdateRepository updateRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUpdateMapper updateMapper;
    private final NotificationService notificationService;

    public ProjectUpdateResponse createUpdate(CreateProjectUpdateRequest request, UUID projectId, UUID creatorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateUpdateCreation(project, creator);

        ProjectUpdate update = updateMapper.toEntity(request);
        update.setProject(project);
        update.setCreatedBy(creator);
        update.setUpdateType(UpdateType.valueOf(request.getUpdateType()));

        ProjectUpdate savedUpdate = updateRepository.save(update);

        notifyProjectBackers(savedUpdate);

        return updateMapper.toResponse(savedUpdate);
    }

    public List<ProjectUpdate> getProjectUpdates(UUID projectId) {
        return updateRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public ProjectUpdateResponse getUpdate(UUID updateId) {
        ProjectUpdate update = updateRepository.findById(updateId)
                .orElseThrow(() -> new ResourceNotFoundException("Update not found"));
        return updateMapper.toResponse(update);
    }

    public List<ProjectUpdateResponse> getUpdatesByType(UUID projectId, UpdateType type) {
        return updateRepository.findByProjectIdAndUpdateType(projectId, type)
                .stream()
                .map(updateMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectUpdateResponse> getMilestoneUpdates(UUID projectId) {
        return getUpdatesByType(projectId, UpdateType.MILESTONE);
    }

    public List<ProjectUpdateResponse> getAnnouncementUpdates(UUID projectId) {
        return getUpdatesByType(projectId, UpdateType.ANNOUNCEMENT);
    }

    private void validateUpdateCreation(Project project, User creator) {
        // Check if user is project creator or team member
        boolean isAuthorized = project.getCreator().getId().equals(creator.getId()) ||
                project.getProjectTeam().stream()
                        .anyMatch(member -> member.getUser().getId().equals(creator.getId()));

        if (!isAuthorized) {
            throw new BusinessValidationException("Not authorized to create project updates");
        }
    }

    private void notifyProjectBackers(ProjectUpdate update) {
        Project project = update.getProject();

        // Get unique backers for the project
        project.getPledges().stream()
                .map(pledge -> pledge.getBacker().getId())
                .distinct()
                .forEach(backerId ->
                        notificationService.createNotification(
                                backerId,
                                String.format("New update for project '%s': %s",
                                        project.getTitle(),
                                        update.getTitle()),
                                "PROJECT_UPDATE",
                                update.getId()
                        )
                );
    }
}