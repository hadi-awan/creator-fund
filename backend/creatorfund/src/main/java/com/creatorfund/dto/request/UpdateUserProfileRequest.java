package com.creatorfund.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserProfileRequest {
    private String location;
    private String website;
    private String socialLinks;
    private List<String> skills;
    private List<String> interests;
}
