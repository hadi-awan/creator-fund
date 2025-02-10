package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.UserProfileMapper;
import com.creatorfund.model.User;
import com.creatorfund.model.UserProfile;
import com.creatorfund.model.UserSkill;
import com.creatorfund.model.UserInterest;
import com.creatorfund.repository.UserProfileRepository;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class UserProfileServiceTest extends BaseServiceTest {
    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(
                userProfileRepository,
                userRepository,
                userProfileMapper
        );
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));
        when(userProfileMapper.toResponse(userProfile)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userProfileService.getUserProfile(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedResponse.getId());
        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    void getUserProfile_NotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userProfileService.getUserProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User profile not found");
    }

    @Test
    void updateUserProfile_ExistingProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = createSampleUser(userId);
        UserProfile existingProfile = createSampleUserProfile();
        existingProfile.setUser(user);
        UpdateUserProfileRequest request = createSampleUpdateProfileRequest();
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(existingProfile);
        when(userProfileMapper.toResponse(existingProfile)).thenReturn(expectedResponse);
        doNothing().when(userProfileMapper).updateFromRequest(existingProfile, request);

        // Act
        UserProfileResponse result = userProfileService.updateUserProfile(userId, request);

        // Assert
        assertThat(result).isNotNull();
        verify(userProfileRepository).save(existingProfile);
        verify(userProfileMapper).updateFromRequest(existingProfile, request);
    }

    @Test
    void updateUserProfile_NewProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = createSampleUser(userId);
        UpdateUserProfileRequest request = createSampleUpdateProfileRequest();
        UserProfile newProfile = new UserProfile();
        newProfile.setUser(user);
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(newProfile);
        when(userProfileMapper.toResponse(newProfile)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userProfileService.updateUserProfile(userId, request);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileRepository).save(argThat(profile ->
                profile.getUser() != null &&
                        profile.getUser().getId().equals(userId)
        ));
        verify(userProfileMapper).updateFromRequest(newProfile, request);
        verify(userProfileMapper).toResponse(newProfile);
    }

    @Test
    void findUsersBySkill_Success() {
        // Arrange
        String skillName = "Java";
        List<UserProfile> profiles = List.of(createSampleUserProfile());
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userProfileRepository.findBySkills_Skill(skillName)).thenReturn(profiles);
        when(userProfileMapper.toResponse(profiles.get(0))).thenReturn(expectedResponse);

        // Act
        List<UserProfileResponse> results = userProfileService.findUsersBySkill(skillName);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(expectedResponse);
    }

    @Test
    void findUsersByInterest_Success() {
        // Arrange
        String interestName = "Technology";
        List<UserProfile> profiles = List.of(createSampleUserProfile());
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userProfileRepository.findByInterests_Interest(interestName)).thenReturn(profiles);
        when(userProfileMapper.toResponse(profiles.get(0))).thenReturn(expectedResponse);

        // Act
        List<UserProfileResponse> results = userProfileService.findUsersByInterest(interestName);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(expectedResponse);
    }

    @Test
    void addSkill_NewSkill() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        userProfile.setSkills(new ArrayList<>());
        String skillName = "Python";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        // Act
        userProfileService.addSkill(userId, skillName);

        // Assert
        verify(userProfileRepository).save(userProfile);
        assertThat(userProfile.getSkills()).hasSize(1);
        assertThat(userProfile.getSkills().get(0).getSkill()).isEqualTo(skillName);
    }

    @Test
    void addSkill_DuplicateSkill() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        UserSkill existingSkill = new UserSkill();
        existingSkill.setSkill("Python");
        userProfile.setSkills(List.of(existingSkill));
        String skillName = "Python";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));

        // Act
        userProfileService.addSkill(userId, skillName);

        // Assert
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void removeSkill_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        UserSkill skill1 = new UserSkill();
        skill1.setSkill("Java");
        UserSkill skill2 = new UserSkill();
        skill2.setSkill("Python");
        userProfile.setSkills(new ArrayList<>(Arrays.asList(skill1, skill2)));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        // Act
        userProfileService.removeSkill(userId, "Java");

        // Assert
        verify(userProfileRepository).save(userProfile);
        assertThat(userProfile.getSkills()).hasSize(1);
        assertThat(userProfile.getSkills().get(0).getSkill()).isEqualTo("Python");
    }

    @Test
    void addInterest_NewInterest() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        userProfile.setInterests(new ArrayList<>());
        String interestName = "Machine Learning";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        // Act
        userProfileService.addInterest(userId, interestName);

        // Assert
        verify(userProfileRepository).save(userProfile);
        assertThat(userProfile.getInterests()).hasSize(1);
        assertThat(userProfile.getInterests().get(0).getInterest()).isEqualTo(interestName);
    }

    @Test
    void addInterest_DuplicateInterest() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        UserInterest existingInterest = new UserInterest();
        existingInterest.setInterest("Technology");
        userProfile.setInterests(List.of(existingInterest));
        String interestName = "Technology";

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));

        // Act
        userProfileService.addInterest(userId, interestName);

        // Assert
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void removeInterest_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserProfile userProfile = createSampleUserProfile();
        UserInterest interest1 = new UserInterest();
        interest1.setInterest("Technology");
        UserInterest interest2 = new UserInterest();
        interest2.setInterest("Innovation");
        userProfile.setInterests(new ArrayList<>(Arrays.asList(interest1, interest2)));

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        // Act
        userProfileService.removeInterest(userId, "Technology");

        // Assert
        verify(userProfileRepository).save(userProfile);
        assertThat(userProfile.getInterests()).hasSize(1);
        assertThat(userProfile.getInterests().get(0).getInterest()).isEqualTo("Innovation");
    }

    // Helper methods to create test data
    private User createSampleUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        return user;
    }

    private UserProfile createSampleUserProfile() {
        UserProfile profile = new UserProfile();
        profile.setId(UUID.randomUUID());

        UserSkill skill = new UserSkill();
        skill.setSkill("Java");
        profile.setSkills(List.of(skill));

        UserInterest interest = new UserInterest();
        interest.setInterest("Technology");
        profile.setInterests(List.of(interest));

        profile.setLocation("New York");
        profile.setWebsite("https://example.com");
        return profile;
    }

    private UpdateUserProfileRequest createSampleUpdateProfileRequest() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setLocation("New York");
        request.setWebsite("https://example.com");
        request.setSkills(Arrays.asList("Java", "Spring"));
        request.setInterests(Arrays.asList("Technology", "Innovation"));
        return request;
    }

    private UserProfileResponse createSampleUserProfileResponse() {
        return UserProfileResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .location("New York")
                .website("https://example.com")
                .build();
    }
}
