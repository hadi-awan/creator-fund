package com.creatorfund.service;

import com.creatorfund.dto.request.CreatePledgeRequest;
import com.creatorfund.dto.response.PledgeResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.PledgeMapper;
import com.creatorfund.model.Pledge;
import com.creatorfund.model.Project;
import com.creatorfund.model.RewardTier;
import com.creatorfund.model.User;
import com.creatorfund.repository.PledgeRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.RewardTierRepository;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PledgeService {
    private final PledgeRepository pledgeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RewardTierRepository rewardTierRepository;
    private final PledgeMapper pledgeMapper;
    private final TransactionService transactionService;

    public PledgeResponse createPledge(CreatePledgeRequest request, UUID backerId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User backer = userRepository.findById(backerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RewardTier rewardTier = null;
        if (request.getRewardTierId() != null) {
            rewardTier = rewardTierRepository.findById(request.getRewardTierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reward tier not found"));
            validateRewardTier(rewardTier, request.getAmount());
        }

        Pledge pledge = pledgeMapper.toEntity(request);
        pledge.setBacker(backer);
        pledge.setProject(project);
        pledge.setRewardTier(rewardTier);

        Pledge savedPledge = pledgeRepository.save(pledge);

        // Create transaction for the pledge
        transactionService.createTransactionForPledge(savedPledge);

        return pledgeMapper.toResponse(savedPledge);
    }

    private void validateRewardTier(RewardTier rewardTier, BigDecimal pledgeAmount) {
        if (pledgeAmount.compareTo(rewardTier.getAmount()) < 0) {
            throw new IllegalStateException("Pledge amount is less than reward tier minimum");
        }
        if (rewardTier.getLimitCount() != null &&
                rewardTier.getCurrentBackers() >= rewardTier.getLimitCount()) {
            throw new IllegalStateException("Reward tier is no longer available");
        }
    }
}
