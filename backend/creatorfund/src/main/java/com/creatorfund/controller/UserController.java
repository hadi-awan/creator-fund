package com.creatorfund.controller;

import com.creatorfund.dto.request.CreateUserRequest;
import com.creatorfund.dto.request.UpdatePasswordRequest;
import com.creatorfund.dto.response.PledgeResponse;
import com.creatorfund.dto.response.ProjectSummaryResponse;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.model.UserStatus;
import com.creatorfund.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/projects")
    public ResponseEntity<List<ProjectSummaryResponse>> getUserProjects(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserProjects(userId));
    }

    @GetMapping("/{userId}/pledges")
    public ResponseEntity<List<PledgeResponse>> getUserPledges(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserPledges(userId));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam UserStatus status) {
        userService.updateStatus(userId, status);
        return ResponseEntity.ok().build();
    }
}