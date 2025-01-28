package com.creatorfund.dto.request;

import lombok.Data;

@Data
public class UpdateProjectTeamRequest {
    private String role;
    private String permissions;
}
