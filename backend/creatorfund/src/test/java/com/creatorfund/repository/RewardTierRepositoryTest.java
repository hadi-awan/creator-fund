package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RewardTierRepositoryTest {

    @Autowired
    private RewardTierRepository rewardTierRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    private Project project;

    @BeforeEach
    void setUp() {
        // Create user
        User creator = new User();
        creator.setEmail("creator@test.com");
        creator.setFullName("Test Creator");
        creator.setPasswordHash("hashedPassword");
        userRepository.save(creator);

        // Create category
        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        categoryRepository.save(category);

        // Create project
        project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        project.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(project);
    }

    @Test
    void shouldFindRewardTiersOrderedByAmount() {
        // Given
        RewardTier lowTier = createRewardTier("Basic", new BigDecimal("10.00"), 100);
        RewardTier midTier = createRewardTier("Standard", new BigDecimal("50.00"), 50);
        RewardTier highTier = createRewardTier("Premium", new BigDecimal("100.00"), 25);

        // When
        List<RewardTier> tiers = rewardTierRepository.findByProjectIdOrderByAmountAsc(project.getId());

        // Then
        assertThat(tiers).hasSize(3);
        assertThat(tiers.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(tiers.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(tiers.get(2).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldFindAvailableRewardTiers() {
        // Given
        RewardTier fullTier = createRewardTier("Full", new BigDecimal("10.00"), 2);
        fullTier.setCurrentBackers(2); // This tier is full
        rewardTierRepository.save(fullTier);

        RewardTier availableTier = createRewardTier("Available", new BigDecimal("20.00"), 5);
        availableTier.setCurrentBackers(2); // This tier has space
        rewardTierRepository.save(availableTier);

        RewardTier unlimitedTier = createRewardTier("Unlimited", new BigDecimal("30.00"), null);
        rewardTierRepository.save(unlimitedTier);

        // When
        List<RewardTier> availableTiers = rewardTierRepository.findAvailableByProjectId(project.getId());

        // Then
        assertThat(availableTiers).hasSize(2);
        assertThat(availableTiers).extracting("title")
                .containsExactlyInAnyOrder("Available", "Unlimited");
    }

    private RewardTier createRewardTier(String title, BigDecimal amount, Integer limitCount) {
        RewardTier tier = new RewardTier();
        tier.setProject(project);
        tier.setTitle(title);
        tier.setDescription(title + " reward description");
        tier.setAmount(amount);
        tier.setLimitCount(limitCount);
        tier.setCurrentBackers(0);
        tier.setEstimatedDeliveryDate(LocalDate.now().plusMonths(1));
        return rewardTierRepository.save(tier);
    }
}
