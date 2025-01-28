package com.creatorfund.repository;

import com.creatorfund.model.Milestone;
import com.creatorfund.model.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByProjectId(UUID projectId);

    List<Milestone> findByProjectIdAndStatus(UUID projectId, MilestoneStatus status);

    List<Milestone> findByProjectIdOrderByTargetDateAsc(UUID projectId);

    List<Milestone> findByTargetDateBeforeAndStatus(LocalDate date, MilestoneStatus status);

    // Find upcoming milestones
    List<Milestone> findByTargetDateBetweenOrderByTargetDateAsc(LocalDate startDate, LocalDate endDate);

    // Find overdue milestones
    List<Milestone> findByTargetDateBeforeAndStatusNot(LocalDate date, MilestoneStatus status);
}