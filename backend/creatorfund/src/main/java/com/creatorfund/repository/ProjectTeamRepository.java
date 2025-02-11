package com.creatorfund.repository;

import com.creatorfund.model.ProjectTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectTeamRepository extends JpaRepository<ProjectTeam, UUID> {

    List<ProjectTeam> findByProjectId(UUID projectId);

    List<ProjectTeam> findByUserId(UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
}
