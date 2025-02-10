package com.creatorfund.repository;

import com.creatorfund.model.Message;
import com.creatorfund.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MessageRepositoryTest {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindConversation() {
        // Given
        User user1 = createTestUser("user1@test.com");
        User user2 = createTestUser("user2@test.com");

        Message message1 = new Message();
        message1.setSender(user1);
        message1.setRecipient(user2);
        message1.setContent("Hello");
        messageRepository.save(message1);

        Message message2 = new Message();
        message2.setSender(user2);
        message2.setRecipient(user1);
        message2.setContent("Hi there");
        messageRepository.save(message2);

        // When
        List<Message> conversation = messageRepository
                .findConversation(user1.getId(), user2.getId());

        // Then
        assertThat(conversation).hasSize(2);
        assertThat(conversation)
                .extracting(Message::getContent)
                .containsExactlyInAnyOrder("Hello", "Hi there");
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }
}