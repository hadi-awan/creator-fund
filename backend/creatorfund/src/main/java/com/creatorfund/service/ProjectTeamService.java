package com.creatorfund.service;

import com.creatorfund.dto.request.CreateProjectTeamRequest;
import com.creatorfund.dto.request.UpdateProjectTeamRequest;
import com.creatorfund.dto.response.ProjectTeamMemberResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.ProjectTeamMapper;
import com.creatorfund.model.Project;
import com.creatorfund.model.ProjectTeam;
import com.creatorfund.model.TeamRole;
import com.creatorfund.model.User;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.ProjectTeamRepository;
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
public class ProjectTeamService {

    private final ProjectTeamRepository projectTeamRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectTeamMapper projectTeamMapper;
    private final NotificationService notificationService;

    public ProjectTeamMemberResponse addTeamMember(CreateProjectTeamRequest request, UUID addedBy) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateTeamMemberAddition(project, user, addedBy);

        ProjectTeam projectTeam = projectTeamMapper.toEntity(request);
        projectTeam.setProject(project);
        projectTeam.setUser(user);

        ProjectTeam savedTeamMember = projectTeamRepository.save(projectTeam);

        notifyTeamMemberAdded(savedTeamMember);

        return projectTeamMapper.toResponse(savedTeamMember);
    }

    public List<ProjectTeamMemberResponse> getProjectTeam(UUID projectId) {
        return projectTeamRepository.findByProjectId(projectId)
                .stream()
                .map(projectTeamMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectTeamMemberResponse updateTeamMember(UUID teamMemberId,
                                                      UpdateProjectTeamRequest request,
                                                      UUID updatedBy) {
        ProjectTeam teamMember = projectTeamRepository.findById(teamMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        validateTeamMemberUpdate(teamMember.getProject(), updatedBy);

        teamMember.setRole(TeamRole.valueOf(request.getRole()));
        teamMember.setPermissions(request.getPermissions());

        ProjectTeam updatedMember = projectTeamRepository.save(teamMember);
        return projectTeamMapper.toResponse(updatedMember);
    }

    public void removeTeamMember(UUID teamMemberId, UUID removedBy) {
        ProjectTeam teamMember = projectTeamRepository.findById(teamMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        validateTeamMemberRemoval(teamMember.getProject(), removedBy);

        projectTeamRepository.delete(teamMember);

        notifyTeamMemberRemoved(teamMember);
    }

    public List<ProjectTeamMemberResponse> getUserProjects(UUID userId) {
        return projectTeamRepository.findByUserId(userId)
                .stream()
                .map(projectTeamMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateTeamMemberAddition(Project project, User user, UUID addedBy) {
        // Check if the user adding has permission (project creator or admin)
        if (!project.getCreator().getId().equals(addedBy) &&
                isTeamAdmin(project, addedBy)) {
            throw new BusinessValidationException(
                    "Only project creator or admin can add team members");
        }

        // Check if user is already a team member
        if (projectTeamRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            throw new BusinessValidationException("User is already a team member");
        }
    }

    private void validateTeamMemberUpdate(Project project, UUID updatedBy) {
        if (!project.getCreator().getId().equals(updatedBy) &&
                isTeamAdmin(project, updatedBy)) {
            throw new BusinessValidationException(
                    "Only project creator or admin can update team members");
        }
    }

    private void validateTeamMemberRemoval(Project project, UUID removedBy) {
        if (!project.getCreator().getId().equals(removedBy) &&
                isTeamAdmin(project, removedBy)) {
            throw new BusinessValidationException(
                    "Only project creator or admin can remove team members");
        }
    }

    private boolean isTeamAdmin(Project project, UUID userId) {
        return project.getProjectTeam().stream()
                .noneMatch(member ->
                        member.getUser().getId().equals(userId) &&
                                member.getRole() == TeamRole.ADMIN);
    }

    private void notifyTeamMemberAdded(ProjectTeam teamMember) {
        notificationService.createNotification(
                teamMember.getUser().getId(),
                String.format("You have been added to the project '%s' team",
                        teamMember.getProject().getTitle()),
                "TEAM_MEMBER_ADDED",
                teamMember.getProject().getId()
        );
    }

    private void notifyTeamMemberRemoved(ProjectTeam teamMember) {
        notificationService.createNotification(
                teamMember.getUser().getId(),
                String.format("You have been removed from the project '%s' team",
                        teamMember.getProject().getTitle()),
                "TEAM_MEMBER_REMOVED",
                teamMember.getProject().getId()
        );
    }
}
