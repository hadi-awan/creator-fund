package com.creatorfund.mapper;

import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.model.UserProfile;
import com.creatorfund.model.UserSkill;
import com.creatorfund.model.UserInterest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserProfileMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "bio", source = "user.bio")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "socialLinks", expression = "java(mapSocialLinksToMap(userProfile.getSocialLinks()))")
    @Mapping(target = "skills", expression = "java(mapSkillsToList(userProfile.getSkills()))")
    @Mapping(target = "interests", expression = "java(mapInterestsToList(userProfile.getInterests()))")
    UserProfileResponse toResponse(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "skills", expression = "java(mapToUserSkills(request.getSkills(), userProfile))")
    @Mapping(target = "interests", expression = "java(mapToUserInterests(request.getInterests(), userProfile))")
    void updateFromRequest(@MappingTarget UserProfile userProfile, UpdateUserProfileRequest request);

    default Map<String, String> mapSocialLinksToMap(String socialLinks) {
        if (socialLinks == null || socialLinks.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(socialLinks, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    default List<String> mapSkillsToList(List<UserSkill> skills) {
        if (skills == null) {
            return Collections.emptyList();
        }
        return skills.stream()
                .map(UserSkill::getSkill)
                .collect(Collectors.toList());
    }

    default List<String> mapInterestsToList(List<UserInterest> interests) {
        if (interests == null) {
            return Collections.emptyList();
        }
        return interests.stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toList());
    }

    default List<UserSkill> mapToUserSkills(List<String> skills, UserProfile userProfile) {
        if (skills == null) {
            return new ArrayList<>();
        }
        return skills.stream()
                .map(skill -> {
                    UserSkill userSkill = new UserSkill();
                    userSkill.setSkill(skill);
                    userSkill.setUserProfile(userProfile);
                    return userSkill;
                })
                .collect(Collectors.toList());
    }

    default List<UserInterest> mapToUserInterests(List<String> interests, UserProfile userProfile) {
        if (interests == null) {
            return new ArrayList<>();
        }
        return interests.stream()
                .map(interest -> {
                    UserInterest userInterest = new UserInterest();
                    userInterest.setInterest(interest);
                    userInterest.setUserProfile(userProfile);
                    return userInterest;
                })
                .collect(Collectors.toList());
    }
}