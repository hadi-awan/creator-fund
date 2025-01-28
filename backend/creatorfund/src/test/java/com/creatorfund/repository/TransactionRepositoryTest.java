package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PledgeRepository pledgeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    @Test
    void shouldFindTransactionsByDateRange() {
        // Given
        Project project = createTestProject();
        User backer = createTestUser("backer@test.com");
        Pledge pledge = createTestPledge(project, backer);

        // Create first transaction with payment attempt
        Transaction transaction1 = new Transaction();
        transaction1.setPledge(pledge);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setStatus("PENDING");
        transaction1.setPaymentProviderRef("TX_REF_001");
        transaction1.setCreatedAt(ZonedDateTime.now().minusDays(1));
        transactionRepository.save(transaction1);

        // Create second transaction for the same pledge (e.g., successful payment after initial failure)
        Transaction transaction2 = new Transaction();
        transaction2.setPledge(pledge);
        transaction2.setAmount(new BigDecimal("100.00"));
        transaction2.setStatus("COMPLETED");
        transaction2.setPaymentProviderRef("TX_REF_002");
        transaction2.setCreatedAt(ZonedDateTime.now());
        transactionRepository.save(transaction2);

        // When
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(
                ZonedDateTime.now().minusDays(2),
                ZonedDateTime.now().plusDays(1)
        );

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions)
                .extracting(Transaction::getStatus)
                .containsExactlyInAnyOrder("PENDING", "COMPLETED");
        assertThat(transactions)
                .extracting(Transaction::getPaymentProviderRef)
                .containsExactlyInAnyOrder("TX_REF_001", "TX_REF_002");
    }

    private Project createTestProject() {
        User creator = createTestUser("creator@test.com");

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        categoryRepository.save(category);

        Project project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        return projectRepository.save(project);
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }

    private Pledge createTestPledge(Project project, User backer) {
        Pledge pledge = new Pledge();
        pledge.setProject(project);
        pledge.setBacker(backer);
        pledge.setAmount(new BigDecimal("100.00"));
        pledge.setStatus(PledgeStatus.SUCCESSFUL);
        return pledgeRepository.save(pledge);
    }
}