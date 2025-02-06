package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateUserRequest;
import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.UserMapper;
import com.creatorfund.model.User;
import com.creatorfund.model.UserProfile;
import com.creatorfund.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper);
    }

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserRequest request = createSampleUserRequest();
        User user = createSampleUser();
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userService.createUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(expectedResponse.getEmail());
        assertThat(result.getFullName()).isEqualTo(expectedResponse.getFullName());

        verify(userRepository).save(any(User.class));
        verify(userMapper).toProfileResponse(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        // Arrange
        CreateUserRequest request = createSampleUserRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = createSampleUser();
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userService.getUser(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedResponse.getId());
        verify(userMapper).toProfileResponse(user);
    }

    @Test
    void getUser_NotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UpdateUserProfileRequest request = createSampleUpdateProfileRequest();
        User user = createSampleUser();
        UserProfile userProfile = createSampleUserProfile(user);
        user.setUserProfile(userProfile);
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userService.updateProfile(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLocation()).isEqualTo(expectedResponse.getLocation());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_CreateNewProfile() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UpdateUserProfileRequest request = createSampleUpdateProfileRequest();
        User user = createSampleUser();
        UserProfileResponse expectedResponse = createSampleUserProfileResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        // Act
        UserProfileResponse result = userService.updateProfile(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(user.getUserProfile()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // Helper methods to create test data
    private CreateUserRequest createSampleUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setBio("Test Bio");
        return request;
    }

    private UpdateUserProfileRequest createSampleUpdateProfileRequest() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setLocation("New York");
        request.setWebsite("https://example.com");
        request.setSocialLinks("{}");
        request.setSkills(Arrays.asList("Java", "Spring"));
        request.setInterests(Arrays.asList("Technology", "Innovation"));
        return request;
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setBio("Test Bio");
        user.setCreatedAt(ZonedDateTime.now());
        return user;
    }

    private UserProfile createSampleUserProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setLocation("New York");
        profile.setWebsite("https://example.com");
        profile.setSocialLinks("{}");
        return profile;
    }

    private UserProfileResponse createSampleUserProfileResponse() {
        return UserProfileResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .bio("Test Bio")
                .location("New York")
                .website("https://example.com")
                .build();
    }
}