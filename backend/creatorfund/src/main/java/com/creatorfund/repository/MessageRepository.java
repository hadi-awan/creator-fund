package com.creatorfund.repository;

import com.creatorfund.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
            "(m.sender.id = :user1Id AND m.recipient.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.recipient.id = :user1Id) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findConversation(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}
