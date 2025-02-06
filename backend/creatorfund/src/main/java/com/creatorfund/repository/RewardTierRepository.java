package com.creatorfund.repository;

import com.creatorfund.model.RewardTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RewardTierRepository extends JpaRepository<RewardTier, UUID> {

    List<RewardTier> findByProjectIdOrderByAmountAsc(UUID projectId);

    @Query("SELECT r FROM RewardTier r WHERE r.project.id = ?1 AND (r.currentBackers < r.limitCount OR r.limitCount IS NULL)")
    List<RewardTier> findAvailableByProjectId(UUID projectId);

    @Query("SELECT r FROM RewardTier r WHERE r.project.id = :projectId " +
            "AND (r.limitCount IS NULL OR r.currentBackers < r.limitCount)")
    List<RewardTier> findByProjectIdAndCurrentBackersLessThanLimitCount(UUID projectId);
}
