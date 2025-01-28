package com.creatorfund.dto.response;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String bio;
    private String profileImageUrl;
    private String location;
    private String website;
    private Map<String, String> socialLinks;
    private List<String> skills;
    private List<String> interests;
    private UserStatsResponse stats;
    private ZonedDateTime createdAt;
}
