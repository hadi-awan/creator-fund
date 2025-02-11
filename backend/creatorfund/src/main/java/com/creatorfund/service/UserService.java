package com.creatorfund.service;

import com.creatorfund.dto.request.CreateUserRequest;
import com.creatorfund.dto.request.UpdatePasswordRequest;
import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.PledgeResponse;
import com.creatorfund.dto.response.ProjectSummaryResponse;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.PledgeMapper;
import com.creatorfund.mapper.ProjectMapper;
import com.creatorfund.mapper.UserMapper;
import com.creatorfund.model.User;
import com.creatorfund.model.UserProfile;
import com.creatorfund.model.UserStatus;
import com.creatorfund.repository.PledgeRepository;
import com.creatorfund.repository.ProjectRepository;
import com.creatorfund.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final PledgeRepository pledgeRepository;
    private final PledgeMapper pledgeMapper;
    private final PasswordEncoder passwordEncoder;

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

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has active projects or pledges
        if (!user.getProjects().isEmpty()) {
            throw new BusinessValidationException("Cannot delete user with active projects");
        }

        userRepository.delete(user);
    }

    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessValidationException("Invalid old password");
        }

        // Update with new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<ProjectSummaryResponse> getUserProjects(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return projectRepository.findByCreatorId(userId).stream()
                .map(projectMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public List<PledgeResponse> getUserPledges(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return pledgeRepository.findByBackerId(userId).stream()
                .map(pledgeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void updateStatus(UUID userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(status);
        userRepository.save(user);
    }
}
