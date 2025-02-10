package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateRefundRequest;
import com.creatorfund.dto.response.RefundResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.mapper.RefundMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.RefundRepository;
import com.creatorfund.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefundServiceTest extends BaseServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RefundMapper refundMapper;

    @Mock
    private NotificationService notificationService;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(
                refundRepository,
                transactionRepository,
                refundMapper,
                notificationService
        );
    }

    @Test
    void createRefund_Success() {
        // Arrange
        CreateRefundRequest request = createSampleRefundRequest();
        Transaction transaction = createSampleTransaction();
        Refund refund = createSampleRefund(transaction);
        RefundResponse expectedResponse = createSampleRefundResponse();

        when(transactionRepository.findById(request.getTransactionId())).thenReturn(Optional.of(transaction));
        when(refundMapper.toEntity(request)).thenReturn(refund);
        when(refundRepository.save(any(Refund.class))).thenReturn(refund);
        when(refundMapper.toResponse(refund)).thenReturn(expectedResponse);

        // Act
        RefundResponse result = refundService.createRefund(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(expectedResponse.getAmount());
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void createRefund_AmountExceedsTransaction() {
        // Arrange
        CreateRefundRequest request = createSampleRefundRequest();
        request.setAmount(new BigDecimal("2000.00")); // More than transaction amount
        Transaction transaction = createSampleTransaction();

        when(transactionRepository.findById(request.getTransactionId())).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThatThrownBy(() -> refundService.createRefund(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Refund amount cannot exceed transaction amount");
    }

    // Helper methods
    private CreateRefundRequest createSampleRefundRequest() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setTransactionId(UUID.randomUUID());
        request.setAmount(new BigDecimal("100.00"));
        request.setReason("Customer request");
        return request;
    }

    private Transaction createSampleTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus("COMPLETED");
        transaction.setPledge(createSamplePledge());
        return transaction;
    }

    private Pledge createSamplePledge() {
        Pledge pledge = new Pledge();
        pledge.setId(UUID.randomUUID());
        pledge.setAmount(new BigDecimal("1000.00"));
        pledge.setStatus(PledgeStatus.SUCCESSFUL);
        pledge.setBacker(createSampleUser());
        pledge.setProject(createSampleProject());
        return pledge;
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
        return project;
    }

    private Refund createSampleRefund(Transaction transaction) {
        Refund refund = new Refund();
        refund.setId(UUID.randomUUID());
        refund.setTransaction(transaction);
        refund.setAmount(new BigDecimal("100.00"));
        refund.setReason("Customer request");
        refund.setStatus("PENDING");
        refund.setCreatedAt(ZonedDateTime.now());
        return refund;
    }

    private RefundResponse createSampleRefundResponse() {
        return RefundResponse.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .reason("Customer request")
                .status("PENDING")
                .createdAt(ZonedDateTime.now())
                .build();
    }
}