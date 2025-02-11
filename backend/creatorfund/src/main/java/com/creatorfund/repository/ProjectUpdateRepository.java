package com.creatorfund.repository;

import com.creatorfund.model.ProjectUpdate;
import com.creatorfund.model.UpdateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, UUID> {

    List<ProjectUpdate> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<ProjectUpdate> findByProjectIdAndUpdateType(UUID projectId, UpdateType type);
}