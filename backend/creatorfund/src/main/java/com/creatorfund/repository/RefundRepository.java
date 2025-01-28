package com.creatorfund.repository;

import com.creatorfund.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByTransactionId(UUID transactionId);

    List<Refund> findByStatus(String status);
}
