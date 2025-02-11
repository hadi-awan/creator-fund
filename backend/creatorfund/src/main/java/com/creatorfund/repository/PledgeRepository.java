package com.creatorfund.repository;

import com.creatorfund.model.Pledge;
import com.creatorfund.model.PledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface PledgeRepository extends JpaRepository<Pledge, UUID> {

    List<Pledge> findByProjectId(UUID projectId);

    List<Pledge> findByProjectIdAndStatus(UUID projectId, PledgeStatus status);
}
