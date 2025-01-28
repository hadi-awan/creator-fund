package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RefundRepositoryTest {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PledgeRepository pledgeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    @Test
    void shouldFindRefundsByStatus() {
        // Given
        Project project = createTestProject();
        User backer = createTestUser("backer@test.com");
        Pledge pledge = createTestPledge(project, backer);
        Transaction transaction = createTestTransaction(pledge);

        Refund refund1 = new Refund();
        refund1.setTransaction(transaction);
        refund1.setAmount(new BigDecimal("50.00"));
        refund1.setStatus("PENDING");
        refund1.setReason("Customer request");
        refundRepository.save(refund1);

        Refund refund2 = new Refund();
        refund2.setTransaction(transaction);
        refund2.setAmount(new BigDecimal("30.00"));
        refund2.setStatus("COMPLETED");
        refund2.setReason("Project cancelled");
        refundRepository.save(refund2);

        // When
        List<Refund> pendingRefunds = refundRepository.findByStatus("PENDING");
        List<Refund> completedRefunds = refundRepository.findByStatus("COMPLETED");

        // Then
        assertThat(pendingRefunds).hasSize(1);
        assertThat(completedRefunds).hasSize(1);
        assertThat(pendingRefunds.get(0).getAmount())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(completedRefunds.get(0).getAmount())
                .isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void shouldFindRefundsByTransactionId() {
        // Given
        Project project = createTestProject();
        User backer = createTestUser("backer@test.com");
        Pledge pledge = createTestPledge(project, backer);
        Transaction transaction = createTestTransaction(pledge);

        Refund refund = new Refund();
        refund.setTransaction(transaction);
        refund.setAmount(new BigDecimal("50.00"));
        refund.setStatus("PENDING");
        refund.setReason("Customer request");
        refundRepository.save(refund);

        // When
        List<Refund> refunds = refundRepository.findByTransactionId(transaction.getId());

        // Then
        assertThat(refunds).hasSize(1);
        assertThat(refunds.get(0).getReason()).isEqualTo("Customer request");
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }

    private Project createTestProject() {
        User creator = createTestUser("creator@test.com");

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        projectCategoryRepository.save(category);

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

    private Pledge createTestPledge(Project project, User backer) {
        Pledge pledge = new Pledge();
        pledge.setProject(project);
        pledge.setBacker(backer);
        pledge.setAmount(new BigDecimal("100.00"));
        pledge.setStatus(PledgeStatus.SUCCESSFUL);
        return pledgeRepository.save(pledge);
    }

    private Transaction createTestTransaction(Pledge pledge) {
        Transaction transaction = new Transaction();
        transaction.setPledge(pledge);
        transaction.setAmount(pledge.getAmount());
        transaction.setStatus("COMPLETED");
        return transactionRepository.save(transaction);
    }
}
