package com.creatorfund.service;

import com.creatorfund.dto.request.CreateRefundRequest;
import com.creatorfund.dto.response.RefundResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.RefundMapper;
import com.creatorfund.model.Pledge;
import com.creatorfund.model.Refund;
import com.creatorfund.model.Transaction;
import com.creatorfund.repository.RefundRepository;
import com.creatorfund.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {
    private final RefundRepository refundRepository;
    private final TransactionRepository transactionRepository;
    private final RefundMapper refundMapper;
    private final NotificationService notificationService;

    public RefundResponse createRefund(CreateRefundRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        validateRefundRequest(transaction, request.getAmount());

        Refund refund = refundMapper.toEntity(request);
        refund.setTransaction(transaction);
        refund.setStatus("PENDING");

        Refund savedRefund = refundRepository.save(refund);

        notifyRefundInitiated(savedRefund);

        return refundMapper.toResponse(savedRefund);
    }

    public RefundResponse processRefund(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        validateRefundProcessing(refund);

        refund.setStatus("COMPLETED");
        refund.setProcessedAt(ZonedDateTime.now());

        Refund processedRefund = refundRepository.save(refund);

        notifyRefundProcessed(processedRefund);

        return refundMapper.toResponse(processedRefund);
    }

    public List<RefundResponse> getTransactionRefunds(UUID transactionId) {
        return refundRepository.findByTransactionId(transactionId)
                .stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getProjectRefunds(UUID projectId) {
        return refundRepository.findByProjectId(projectId)
                .stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public RefundResponse cancelRefund(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        validateRefundCancellation(refund);

        refund.setStatus("CANCELLED");
        Refund cancelledRefund = refundRepository.save(refund);

        notifyRefundCancelled(cancelledRefund);

        return refundMapper.toResponse(cancelledRefund);
    }

    private void validateRefundRequest(Transaction transaction, BigDecimal refundAmount) {
        // Check if transaction is refundable
        if (!"COMPLETED".equals(transaction.getStatus())) {
            throw new BusinessValidationException("Transaction is not in a refundable state");
        }

        // Check if refund amount is valid
        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new BusinessValidationException("Refund amount cannot exceed transaction amount");
        }

        // Check if transaction has already been fully refunded
        BigDecimal totalRefunded = refundRepository.findByTransactionId(transaction.getId())
                .stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingRefundable = transaction.getAmount().subtract(totalRefunded);
        if (refundAmount.compareTo(remainingRefundable) > 0) {
            throw new BusinessValidationException("Requested refund amount exceeds refundable amount");
        }
    }

    private void validateRefundProcessing(Refund refund) {
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BusinessValidationException("Only pending refunds can be processed");
        }
    }

    private void validateRefundCancellation(Refund refund) {
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BusinessValidationException("Only pending refunds can be cancelled");
        }
    }

    private void notifyRefundInitiated(Refund refund) {
        Pledge pledge = refund.getTransaction().getPledge();
        notificationService.createNotification(
                pledge.getBacker().getId(),
                String.format("Refund of $%.2f has been initiated for your pledge to %s",
                        refund.getAmount(),
                        pledge.getProject().getTitle()),
                "REFUND_INITIATED",
                refund.getId()
        );
    }

    private void notifyRefundProcessed(Refund refund) {
        Pledge pledge = refund.getTransaction().getPledge();
        notificationService.createNotification(
                pledge.getBacker().getId(),
                String.format("Refund of $%.2f has been processed for your pledge to %s",
                        refund.getAmount(),
                        pledge.getProject().getTitle()),
                "REFUND_PROCESSED",
                refund.getId()
        );
    }

    private void notifyRefundCancelled(Refund refund) {
        Pledge pledge = refund.getTransaction().getPledge();
        notificationService.createNotification(
                pledge.getBacker().getId(),
                String.format("Refund of $%.2f has been cancelled for your pledge to %s",
                        refund.getAmount(),
                        pledge.getProject().getTitle()),
                "REFUND_CANCELLED",
                refund.getId()
        );
    }
}
