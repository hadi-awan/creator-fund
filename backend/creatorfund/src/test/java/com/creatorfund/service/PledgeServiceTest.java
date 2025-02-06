package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreatePledgeRequest;
import com.creatorfund.dto.response.PledgeResponse;
import com.creatorfund.mapper.PledgeMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.PledgeRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.RewardTierRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PledgeServiceTest extends BaseServiceTest {

    @Mock
    private PledgeRepository pledgeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RewardTierRepository rewardTierRepository;

    @Mock
    private PledgeMapper pledgeMapper;

    @Mock
    private TransactionService transactionService;

    private PledgeService pledgeService;

    @BeforeEach
    void setUp() {
        pledgeService = new PledgeService(
                pledgeRepository,
                projectRepository,
                userRepository,
                rewardTierRepository,
                pledgeMapper,
                transactionService
        );
    }

    @Test
    void createPledge_Success() {
        // Arrange
        UUID backerId = UUID.randomUUID();
        CreatePledgeRequest request = createSamplePledgeRequest();
        User backer = createSampleUser();
        Project project = createSampleProject();
        RewardTier rewardTier = createSampleRewardTier(project);
        Pledge pledge = createSamplePledge(backer, project, rewardTier);
        PledgeResponse expectedResponse = createSamplePledgeResponse();

        when(userRepository.findById(backerId)).thenReturn(Optional.of(backer));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(rewardTierRepository.findById(request.getRewardTierId())).thenReturn(Optional.of(rewardTier));
        when(pledgeMapper.toEntity(request)).thenReturn(pledge);
        when(pledgeRepository.save(any(Pledge.class))).thenReturn(pledge);
        when(pledgeMapper.toResponse(pledge)).thenReturn(expectedResponse);
        doNothing().when(transactionService).createTransactionForPledge(any(Pledge.class));

        // Act
        PledgeResponse result = pledgeService.createPledge(request, backerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(expectedResponse.getAmount());
        verify(pledgeRepository).save(any(Pledge.class));
        verify(transactionService).createTransactionForPledge(any(Pledge.class));
    }

    @Test
    void createPledge_InvalidRewardTierAmount() {
        // Arrange
        UUID backerId = UUID.randomUUID();
        CreatePledgeRequest request = createSamplePledgeRequest();
        request.setAmount(BigDecimal.ONE); // Set amount less than reward tier minimum
        User backer = createSampleUser();
        Project project = createSampleProject();
        RewardTier rewardTier = createSampleRewardTier(project);
        rewardTier.setAmount(BigDecimal.TEN); // Set minimum amount for reward tier

        when(userRepository.findById(backerId)).thenReturn(Optional.of(backer));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(rewardTierRepository.findById(request.getRewardTierId())).thenReturn(Optional.of(rewardTier));

        // Act & Assert
        assertThatThrownBy(() -> pledgeService.createPledge(request, backerId))
                .isInstanceOf(IllegalStateException.class)  // Changed from BusinessValidationException
                .hasMessage("Pledge amount is less than reward tier minimum");
    }

    @Test
    void createPledge_RewardTierLimitReached() {
        // Arrange
        UUID backerId = UUID.randomUUID();
        CreatePledgeRequest request = createSamplePledgeRequest();
        User backer = createSampleUser();
        Project project = createSampleProject();
        RewardTier rewardTier = createSampleRewardTier(project);
        rewardTier.setLimitCount(1);
        rewardTier.setCurrentBackers(1);

        when(userRepository.findById(backerId)).thenReturn(Optional.of(backer));
        when(projectRepository.findById(request.getProjectId())).thenReturn(Optional.of(project));
        when(rewardTierRepository.findById(request.getRewardTierId())).thenReturn(Optional.of(rewardTier));

        // Act & Assert
        assertThatThrownBy(() -> pledgeService.createPledge(request, backerId))
                .isInstanceOf(IllegalStateException.class)  // Changed from BusinessValidationException
                .hasMessage("Reward tier is no longer available");
    }

    // Helper methods
    private CreatePledgeRequest createSamplePledgeRequest() {
        CreatePledgeRequest request = new CreatePledgeRequest();
        request.setProjectId(UUID.randomUUID());
        request.setRewardTierId(UUID.randomUUID());
        request.setAmount(new BigDecimal("100.00"));
        request.setAnonymous(false);
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
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(ZonedDateTime.now().minusDays(1));
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        project.setProjectTeam(new ArrayList<>());
        return project;
    }

    private RewardTier createSampleRewardTier(Project project) {
        RewardTier rewardTier = new RewardTier();
        rewardTier.setId(UUID.randomUUID());
        rewardTier.setProject(project);
        rewardTier.setTitle("Test Reward");
        rewardTier.setAmount(new BigDecimal("100.00"));
        rewardTier.setLimitCount(10);
        rewardTier.setCurrentBackers(0);
        return rewardTier;
    }

    private Pledge createSamplePledge(User backer, Project project, RewardTier rewardTier) {
        Pledge pledge = new Pledge();
        pledge.setId(UUID.randomUUID());
        pledge.setBacker(backer);
        pledge.setProject(project);
        pledge.setRewardTier(rewardTier);
        pledge.setAmount(new BigDecimal("100.00"));
        pledge.setStatus(PledgeStatus.PENDING);
        pledge.setAnonymous(false);
        pledge.setCreatedAt(ZonedDateTime.now());
        return pledge;
    }

    private PledgeResponse createSamplePledgeResponse() {
        return PledgeResponse.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .status(PledgeStatus.PENDING.name())
                .createdAt(ZonedDateTime.now())
                .anonymous(false)
                .build();
    }
}