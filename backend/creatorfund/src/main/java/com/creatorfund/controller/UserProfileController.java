package com.creatorfund.controller;

import com.creatorfund.dto.request.UpdateUserProfileRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateUserProfile(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    @PostMapping("/{userId}/skills")
    public ResponseEntity<Void> addSkill(
            @PathVariable UUID userId,
            @RequestParam String skill) {
        userProfileService.addSkill(userId, skill);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/skills/{skill}")
    public ResponseEntity<Void> removeSkill(
            @PathVariable UUID userId,
            @PathVariable String skill) {
        userProfileService.removeSkill(userId, skill);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/interests")
    public ResponseEntity<Void> addInterest(
            @PathVariable UUID userId,
            @RequestParam String interest) {
        userProfileService.addInterest(userId, interest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/interests/{interest}")
    public ResponseEntity<Void> removeInterest(
            @PathVariable UUID userId,
            @PathVariable String interest) {
        userProfileService.removeInterest(userId, interest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/skills/{skill}")
    public ResponseEntity<List<UserProfileResponse>> findUsersBySkill(@PathVariable String skill) {
        return ResponseEntity.ok(userProfileService.findUsersBySkill(skill));
    }

    @GetMapping("/interests/{interest}")
    public ResponseEntity<List<UserProfileResponse>> findUsersByInterest(@PathVariable String interest) {
        return ResponseEntity.ok(userProfileService.findUsersByInterest(interest));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchProfiles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) List<String> interests) {
        return ResponseEntity.ok(userProfileService.searchProfiles(query, location, skills, interests));
    }
}