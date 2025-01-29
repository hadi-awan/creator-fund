package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateProjectTeamRequest;
import com.creatorfund.dto.request.UpdateProjectTeamRequest;
import com.creatorfund.dto.response.ProjectTeamMemberResponse;
import com.creatorfund.model.ProjectTeam;
import com.creatorfund.model.TeamRole;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, imports = {TeamRole.class})
public interface ProjectTeamMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    @Mapping(target = "role", expression = "java(request.getRole() != null ? TeamRole.valueOf(request.getRole()) : null)")
    ProjectTeam toEntity(CreateProjectTeamRequest request);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "role", expression = "java(projectTeam.getRole() != null ? projectTeam.getRole().name() : null)")
    ProjectTeamMemberResponse toResponse(ProjectTeam projectTeam);

    @Mapping(target = "role", expression = "java(request.getRole() != null ? TeamRole.valueOf(request.getRole()) : projectTeam.getRole())")
    void updateEntityFromRequest(@MappingTarget ProjectTeam projectTeam, UpdateProjectTeamRequest request);
}
