package com.creatorfund.service;

import com.creatorfund.dto.request.CreateTransactionRequest;
import com.creatorfund.dto.response.TransactionResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.TransactionMapper;
import com.creatorfund.model.Pledge;
import com.creatorfund.model.Transaction;
import com.creatorfund.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public void createTransactionForPledge(Pledge pledge) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setPledgeId(pledge.getId());
        request.setAmount(pledge.getAmount());
        request.setCurrency("USD"); // Could be configurable
        request.setPaymentMethod("card"); // Should come from payment processing

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setPledge(pledge);

        Transaction savedTransaction = transactionRepository.save(transaction);
        transactionMapper.toResponse(savedTransaction);
    }

    public TransactionResponse getTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return transactionMapper.toResponse(transaction);
    }
}
