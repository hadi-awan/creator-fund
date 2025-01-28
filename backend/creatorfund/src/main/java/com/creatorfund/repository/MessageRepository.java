package com.creatorfund.repository;

import com.creatorfund.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findBySenderId(UUID senderId);

    List<Message> findByRecipientId(UUID recipientId);

    List<Message> findByRecipientIdAndReadStatus(UUID recipientId, boolean readStatus);

    List<Message> findByProjectId(UUID projectId);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = ?1 AND m.recipient = ?2) OR " +
            "(m.sender = ?2 AND m.recipient = ?1) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findConversation(UUID user1Id, UUID user2Id);
}
