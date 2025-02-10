package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateTransactionRequest;
import com.creatorfund.dto.response.TransactionResponse;
import com.creatorfund.mapper.TransactionMapper;
import com.creatorfund.model.*;

import com.creatorfund.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class TransactionServiceTest extends BaseServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void createTransactionForPledge_Success() {
        Pledge pledge = createSamplePledge();
        Transaction transaction = createSampleTransaction(pledge);
        TransactionResponse expectedResponse = createSampleResponse();

        when(transactionMapper.toEntity(any(CreateTransactionRequest.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.createTransactionForPledge(pledge);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getTransaction_Success() {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = createSampleTransaction(createSamplePledge());
        TransactionResponse expectedResponse = createSampleResponse();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.getTransaction(transactionId);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(expectedResponse.getAmount());
    }

    private Pledge createSamplePledge() {
        Pledge pledge = new Pledge();
        pledge.setId(UUID.randomUUID());
        pledge.setAmount(new BigDecimal("100.00"));
        pledge.setStatus(PledgeStatus.PENDING);
        return pledge;
    }

    private Transaction createSampleTransaction(Pledge pledge) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setPledge(pledge);
        transaction.setAmount(pledge.getAmount());
        transaction.setStatus("PENDING");
        transaction.setCreatedAt(ZonedDateTime.now());
        return transaction;
    }

    private TransactionResponse createSampleResponse() {
        return TransactionResponse.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .status("PENDING")
                .createdAt(ZonedDateTime.now())
                .build();
    }
}
