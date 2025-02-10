package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateRewardTierRequest;
import com.creatorfund.dto.response.RewardTierResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.mapper.RewardTierMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.RewardTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class RewardTierServiceTest extends BaseServiceTest {

    @Mock
    private RewardTierRepository rewardTierRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RewardTierMapper rewardTierMapper;

    private RewardTierService rewardTierService;

    @BeforeEach
    void setUp() {
        rewardTierService = new RewardTierService(
                rewardTierRepository,
                projectRepository,
                rewardTierMapper
        );
    }

    @Test
    void createRewardTier_Success() {
        UUID projectId = UUID.randomUUID();
        CreateRewardTierRequest request = createSampleRequest();
        Project project = createSampleProject();
        RewardTier rewardTier = createSampleRewardTier(project);
        RewardTierResponse expectedResponse = createSampleResponse();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(rewardTierMapper.toEntity(request)).thenReturn(rewardTier);
        when(rewardTierRepository.save(any(RewardTier.class))).thenReturn(rewardTier);
        when(rewardTierMapper.toResponse(rewardTier)).thenReturn(expectedResponse);

        RewardTierResponse result = rewardTierService.createRewardTier(projectId, request);

        assertThat(result).isNotNull();
        verify(rewardTierRepository).save(any(RewardTier.class));
    }

    @Test
    void getProjectRewardTiers_Success() {
        UUID projectId = UUID.randomUUID();
        Project project = createSampleProject();
        RewardTier rewardTier = createSampleRewardTier(project);
        List<RewardTierResponse> expectedResponses = Collections.singletonList(createSampleResponse());

        when(rewardTierRepository.findByProjectIdOrderByAmountAsc(projectId))
                .thenReturn(List.of(rewardTier));
        when(rewardTierMapper.toResponse(rewardTier))
                .thenReturn(expectedResponses.get(0));

        List<RewardTierResponse> result = rewardTierService.getProjectRewardTiers(projectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(expectedResponses.get(0).getTitle());
    }

    @Test
    void incrementBackerCount_Success() {
        UUID rewardTierId = UUID.randomUUID();
        RewardTier rewardTier = createSampleRewardTier(createSampleProject());
        rewardTier.setLimitCount(10);
        rewardTier.setCurrentBackers(5);

        when(rewardTierRepository.findById(rewardTierId)).thenReturn(Optional.of(rewardTier));
        when(rewardTierRepository.save(rewardTier)).thenReturn(rewardTier);

        rewardTierService.incrementBackerCount(rewardTierId);

        assertThat(rewardTier.getCurrentBackers()).isEqualTo(6);
        verify(rewardTierRepository).save(rewardTier);
    }

    @Test
    void incrementBackerCount_LimitReached() {
        UUID rewardTierId = UUID.randomUUID();
        RewardTier rewardTier = createSampleRewardTier(createSampleProject());
        rewardTier.setLimitCount(5);
        rewardTier.setCurrentBackers(5);

        when(rewardTierRepository.findById(rewardTierId)).thenReturn(Optional.of(rewardTier));

        assertThatThrownBy(() -> rewardTierService.incrementBackerCount(rewardTierId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reward tier limit reached");
    }

    private CreateRewardTierRequest createSampleRequest() {
        CreateRewardTierRequest request = new CreateRewardTierRequest();
        request.setTitle("Test Reward");
        request.setDescription("Test Description");
        request.setAmount(new BigDecimal("100.00"));
        request.setLimitCount(10);
        return request;
    }

    private Project createSampleProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test Project");
        return project;
    }

    private RewardTier createSampleRewardTier(Project project) {
        RewardTier rewardTier = new RewardTier();
        rewardTier.setId(UUID.randomUUID());
        rewardTier.setProject(project);
        rewardTier.setTitle("Test Reward");
        rewardTier.setDescription("Test Description");
        rewardTier.setAmount(new BigDecimal("100.00"));
        rewardTier.setCurrentBackers(0);
        return rewardTier;
    }

    private RewardTierResponse createSampleResponse() {
        return RewardTierResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Reward")
                .description("Test Description")
                .amount(new BigDecimal("100.00"))
                .currentBackers(0)
                .build();
    }
}
