package com.creatorfund.service;

import com.creatorfund.dto.request.CreateUserRequest;
import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.UserMapper;
import com.creatorfund.model.User;
import com.creatorfund.model.UserProfile;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserProfileResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        // Password hashing would be done here
        User savedUser = userRepository.save(user);
        return userMapper.toProfileResponse(savedUser);
    }

    public UserProfileResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update profile fields
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            user.setUserProfile(profile);
        }

        // Update profile fields
        User updatedUser = userRepository.save(user);
        return userMapper.toProfileResponse(updatedUser);
    }
}
