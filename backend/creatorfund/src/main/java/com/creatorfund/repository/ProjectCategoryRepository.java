package com.creatorfund.repository;

import com.creatorfund.model.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, UUID> {

    List<ProjectCategory> findByParentCategoryId(UUID parentId);

    Optional<ProjectCategory> findByNameIgnoreCase(String name);

    List<ProjectCategory> findByParentCategoryIdIsNull();

    List<ProjectCategory> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
