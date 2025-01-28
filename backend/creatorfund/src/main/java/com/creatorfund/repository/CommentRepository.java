package com.creatorfund.repository;

import com.creatorfund.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comments, UUID> {
    Page<Comments> findByProjectId(UUID projectId, Pageable pageable);
    List<Comments> findByParentCommentId(UUID parentCommentId);

    @Query("SELECT c FROM Comments c WHERE c.project.id = :projectId AND c.parentComment IS NULL")
    List<Comments> findRootCommentsByProjectId(@Param("projectId") UUID projectId);
}
