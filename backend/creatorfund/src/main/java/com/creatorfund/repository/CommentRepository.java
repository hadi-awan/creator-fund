package com.creatorfund.repository;

import com.creatorfund.model.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

    List<Comments> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<Comments> findByParentCommentId(UUID parentCommentId);

    Page<Comments> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT c FROM Comments c WHERE c.project = ?1 AND c.parentComment IS NULL")
    List<Comments> findRootCommentsByProjectId(UUID projectId);
}
