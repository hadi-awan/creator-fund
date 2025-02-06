package com.creatorfund.service;

import com.creatorfund.dto.request.CreateRewardTierRequest;
import com.creatorfund.dto.response.RewardTierResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.RewardTierMapper;
import com.creatorfund.model.Project;
import com.creatorfund.model.ProjectStatus;
import com.creatorfund.model.RewardTier;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.RewardTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardTierService {
    private final RewardTierRepository rewardTierRepository;
    private final ProjectRepository projectRepository;
    private final RewardTierMapper rewardTierMapper;

    public RewardTierResponse createRewardTier(UUID projectId, CreateRewardTierRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateRewardTierCreation(project);

        RewardTier rewardTier = rewardTierMapper.toEntity(request);
        rewardTier.setProject(project);

        RewardTier savedRewardTier = rewardTierRepository.save(rewardTier);
        return rewardTierMapper.toResponse(savedRewardTier);
    }

    public List<RewardTierResponse> getProjectRewardTiers(UUID projectId) {
        return rewardTierRepository.findByProjectIdOrderByAmountAsc(projectId).stream()
                .map(rewardTierMapper::toResponse)
                .toList();
    }

    public List<RewardTierResponse> getAvailableRewardTiers(UUID projectId) {
        return rewardTierRepository.findByProjectIdAndCurrentBackersLessThanLimitCount(projectId)
                .stream()
                .map(rewardTierMapper::toResponse)
                .toList();
    }

    public RewardTierResponse getRewardTier(UUID rewardTierId) {
        RewardTier rewardTier = rewardTierRepository.findById(rewardTierId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward tier not found"));
        return rewardTierMapper.toResponse(rewardTier);
    }

    @Transactional
    public void incrementBackerCount(UUID rewardTierId) {
        RewardTier rewardTier = rewardTierRepository.findById(rewardTierId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward tier not found"));

        if (rewardTier.getLimitCount() != null &&
                rewardTier.getCurrentBackers() >= rewardTier.getLimitCount()) {
            throw new BusinessValidationException("Reward tier limit reached");
        }

        rewardTier.setCurrentBackers(rewardTier.getCurrentBackers() + 1);
        rewardTierRepository.save(rewardTier);
    }

    private void validateRewardTierCreation(Project project) {
        // Can't add reward tiers to completed or cancelled projects
        if (project.getStatus() == ProjectStatus.COMPLETED ||
                project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessValidationException(
                    "Cannot add reward tiers to completed or cancelled projects");
        }
    }
}
