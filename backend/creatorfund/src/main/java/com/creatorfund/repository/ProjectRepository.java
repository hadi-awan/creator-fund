package com.creatorfund.repository;

import com.creatorfund.model.Project;
import com.creatorfund.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByCreatorId(UUID creatorID);

    List<Project> findByStatus(ProjectStatus status);

    Page<Project> findByCategoryId(UUID categoryID, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.currentAmount >= p.fundingGoal AND p.status = 'ACTIVE'")
    List<Project> findFullyFundedActiveProjects();

    Page<Project> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
