package com.creatorfund.repository;

import com.creatorfund.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPledgeId(UUID pledgeId);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByCreatedAtBetween(ZonedDateTime start, ZonedDateTime end);
}
