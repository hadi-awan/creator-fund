package com.creatorfund.service;

import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.UserProfileMapper;
import com.creatorfund.model.*;
import com.creatorfund.repository.UserProfileRepository;
import com.creatorfund.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileResponse getUserProfile(UUID userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return userProfileMapper.toResponse(userProfile);
    }

    public UserProfileResponse updateUserProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        userProfileMapper.updateFromRequest(userProfile, request);
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toResponse(savedProfile);
    }

    public List<UserProfileResponse> findUsersBySkill(String skillName) {
        return userProfileRepository.findBySkills_Skill(skillName).stream()
                .map(userProfileMapper::toResponse)
                .toList();
    }

    public List<UserProfileResponse> findUsersByInterest(String interestName) {
        return userProfileRepository.findByInterests_Interest(interestName).stream()
                .map(userProfileMapper::toResponse)
                .toList();
    }

    public void addSkill(UUID userId, String skillName) {
        UserProfile userProfile = getUserProfileOrCreate(userId);
        if (userProfile.getSkills().stream().noneMatch(s -> s.getSkill().equals(skillName))) {
            UserSkill skill = new UserSkill();
            skill.setUserProfile(userProfile);
            skill.setSkill(skillName);
            userProfile.getSkills().add(skill);
            userProfileRepository.save(userProfile);
        }
    }

    public void removeSkill(UUID userId, String skillName) {
        UserProfile userProfile = getUserProfileOrCreate(userId);
        userProfile.getSkills().removeIf(s -> s.getSkill().equals(skillName));
        userProfileRepository.save(userProfile);
    }

    public void addInterest(UUID userId, String interestName) {
        UserProfile userProfile = getUserProfileOrCreate(userId);
        if (userProfile.getInterests().stream().noneMatch(i -> i.getInterest().equals(interestName))) {
            UserInterest interest = new UserInterest();
            interest.setUserProfile(userProfile);
            interest.setInterest(interestName);
            userProfile.getInterests().add(interest);
            userProfileRepository.save(userProfile);
        }
    }

    public void removeInterest(UUID userId, String interestName) {
        UserProfile userProfile = getUserProfileOrCreate(userId);
        userProfile.getInterests().removeIf(i -> i.getInterest().equals(interestName));
        userProfileRepository.save(userProfile);
    }

    private UserProfile getUserProfileOrCreate(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return userProfileRepository.save(newProfile);
                });
    }

    public List<UserProfileResponse> searchProfiles(String query, String location,
                                                    List<String> skills, List<String> interests) {
        Specification<UserProfile> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")),
                                    "%" + query.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")),
                                    "%" + query.toLowerCase() + "%")
                    )
            );
        }

        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                            "%" + location.toLowerCase() + "%")
            );
        }

        if (skills != null && !skills.isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    root.join("skills").get("skill").in(skills)
            );
        }

        if (interests != null && !interests.isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    root.join("interests").get("interest").in(interests)
            );
        }

        return userProfileRepository.findAll((Sort) spec).stream()
                .map(userProfileMapper::toResponse)
                .collect(Collectors.toList());
    }
}