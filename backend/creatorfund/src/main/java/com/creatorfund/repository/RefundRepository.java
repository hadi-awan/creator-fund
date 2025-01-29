package com.creatorfund.repository;

import com.creatorfund.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByTransactionId(UUID transactionId);

    List<Refund> findByStatus(String status);

    @Query("SELECT r FROM Refund r WHERE r.transaction.pledge.project.id = :projectId")
    List<Refund> findByProjectId(@Param("projectId") UUID projectId);
}
