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

        // Add skills
        UserSkill javaSkill = new UserSkill();
        javaSkill.setSkill("Java");
        javaSkill.setUserProfile(profile);
        profile.getSkills().add(javaSkill);

        UserSkill springSkill = new UserSkill();
        springSkill.setSkill("Spring Boot");
        springSkill.setUserProfile(profile);
        profile.getSkills().add(springSkill);

        // Add interests
        UserInterest techInterest = new UserInterest();
        techInterest.setInterest("Technology");
        techInterest.setUserProfile(profile);
        profile.getInterests().add(techInterest);

        UserInterest innovationInterest = new UserInterest();
        innovationInterest.setInterest("Innovation");
        innovationInterest.setUserProfile(profile);
        profile.getInterests().add(innovationInterest);

        // Save the profile
        userProfileRepository.save(profile);

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

        UserSkill javaSkill1 = new UserSkill();
        javaSkill1.setSkill("Java");
        javaSkill1.setUserProfile(profile1);
        profile1.getSkills().add(javaSkill1);
        userProfileRepository.save(profile1);

        UserProfile profile2 = new UserProfile();
        profile2.setUser(user2);

        UserSkill javaSkill2 = new UserSkill();
        javaSkill2.setSkill("Java");
        javaSkill2.setUserProfile(profile2);
        profile2.getSkills().add(javaSkill2);
        userProfileRepository.save(profile2);

        UserProfile profile3 = new UserProfile();
        User user3 = createTestUser("user3@test.com");
        profile3.setUser(user3);

        UserSkill pythonSkill = new UserSkill();
        pythonSkill.setSkill("Python");
        pythonSkill.setUserProfile(profile3);
        profile3.getSkills().add(pythonSkill);
        userProfileRepository.save(profile3);

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