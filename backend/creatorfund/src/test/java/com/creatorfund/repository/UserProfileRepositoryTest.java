package com.creatorfund.repository;

import com.creatorfund.model.User;
import com.creatorfund.model.UserInterest;
import com.creatorfund.model.UserProfile;
import com.creatorfund.model.UserSkill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserProfileRepositoryTest {
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindProfileByUserId() {
        // Given
        User user = createTestUser("test@example.com");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setLocation("New York");
        profile.setWebsite("https://example.com");

        // Add skills and interests
        profile.addSkill("Java");
        profile.addSkill("Spring Boot");
        profile.addInterest("Technology");
        profile.addInterest("Innovation");

        UserProfile savedProfile = userProfileRepository.save(profile);

        // When
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(user.getId());

        // Then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getLocation()).isEqualTo("New York");
        assertThat(foundProfile.get().getSkills())
                .extracting(UserSkill::getSkill)
                .containsExactlyInAnyOrder("Java", "Spring Boot");
        assertThat(foundProfile.get().getInterests())
                .extracting(UserInterest::getInterest)
                .containsExactlyInAnyOrder("Technology", "Innovation");
    }

    @Test
    void shouldFindProfilesBySkill() {
        // Given
        User user1 = createTestUser("user1@test.com");
        User user2 = createTestUser("user2@test.com");

        UserProfile profile1 = new UserProfile();
        profile1.setUser(user1);
        profile1.addSkill("Java");
        profile1.addSkill("Python");
        userProfileRepository.save(profile1);

        UserProfile profile2 = new UserProfile();
        profile2.setUser(user2);
        profile2.addSkill("Java");
        profile2.addSkill("JavaScript");
        userProfileRepository.save(profile2);

        // When
        List<UserProfile> javaProfiles = userProfileRepository.findBySkill("Java");
        List<UserProfile> pythonProfiles = userProfileRepository.findBySkill("Python");

        // Then
        assertThat(javaProfiles).hasSize(2);
        assertThat(pythonProfiles).hasSize(1);
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test User");
        user.setPasswordHash("hashedPassword");
        return userRepository.save(user);
    }
}
