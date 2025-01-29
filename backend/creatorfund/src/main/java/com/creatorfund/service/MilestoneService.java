package com.creatorfund.service;

import com.creatorfund.dto.request.CreateMilestoneRequest;
import com.creatorfund.dto.response.MilestoneResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.MilestoneMapper;
import com.creatorfund.model.Milestone;
import com.creatorfund.model.MilestoneStatus;
import com.creatorfund.model.Project;
import com.creatorfund.repository.MilestoneRepository;
import com.creatorfund.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;
    private final NotificationService notificationService;

    public MilestoneResponse createMilestone(UUID projectId, CreateMilestoneRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateMilestoneCreation(project, request);

        Milestone milestone = milestoneMapper.toEntity(request);
        milestone.setProject(project);

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(savedMilestone);
    }

    public List<MilestoneResponse> getProjectMilestones(UUID projectId) {
        return milestoneRepository.findByProjectIdOrderByTargetDateAsc(projectId)
                .stream()
                .map(milestoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    public MilestoneResponse completeMilestone(UUID milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));

        validateMilestoneCompletion(milestone);

        milestone.setStatus(MilestoneStatus.COMPLETED);
        milestone.setCompletionDate(LocalDate.now());

        Milestone updatedMilestone = milestoneRepository.save(milestone);

        // Notify project stakeholders
        notifyMilestoneCompleted(updatedMilestone);

        return milestoneMapper.toResponse(updatedMilestone);
    }

    public MilestoneResponse updateMilestoneStatus(UUID milestoneId, MilestoneStatus newStatus) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));

        validateStatusTransition(milestone, newStatus);

        milestone.setStatus(newStatus);
        if (newStatus == MilestoneStatus.COMPLETED) {
            milestone.setCompletionDate(LocalDate.now());
        }

        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updatedMilestone);
    }

    public List<MilestoneResponse> getOverdueMilestones(UUID projectId) {
        return milestoneRepository.findByTargetDateBeforeAndStatusNot(
                        LocalDate.now(),
                        MilestoneStatus.COMPLETED
                )
                .stream()
                .map(milestoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<MilestoneResponse> getUpcomingMilestones(UUID projectId, int daysAhead) {
        LocalDate today = LocalDate.now();
        return milestoneRepository.findByTargetDateBetweenOrderByTargetDateAsc(
                        today,
                        today.plusDays(daysAhead)
                )
                .stream()
                .map(milestoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateMilestoneCreation(Project project, CreateMilestoneRequest request) {
        if (project.getEndDate().toLocalDate().isBefore(request.getTargetDate())) {
            throw new BusinessValidationException(
                    "Milestone target date cannot be after project end date");
        }

        if (request.getTargetDate().isBefore(LocalDate.now())) {
            throw new BusinessValidationException(
                    "Milestone target date cannot be in the past");
        }
    }

    private void validateMilestoneCompletion(Milestone milestone) {
        if (milestone.getStatus() == MilestoneStatus.COMPLETED) {
            throw new BusinessValidationException("Milestone is already completed");
        }
    }

    private void validateStatusTransition(Milestone milestone, MilestoneStatus newStatus) {
        if (milestone.getStatus() == MilestoneStatus.COMPLETED &&
                newStatus != MilestoneStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Cannot change status of a completed milestone");
        }
    }

    private void notifyMilestoneCompleted(Milestone milestone) {
        Project project = milestone.getProject();
        notificationService.createNotification(
                project.getCreator().getId(),
                String.format("Milestone '%s' has been completed", milestone.getTitle()),
                "MILESTONE_COMPLETED",
                milestone.getId()
        );
    }
}
