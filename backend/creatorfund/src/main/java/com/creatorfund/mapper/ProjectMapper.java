package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateProjectRequest;
import com.creatorfund.dto.request.UpdateProjectRequest;
import com.creatorfund.dto.response.ProjectDetailsResponse;
import com.creatorfund.dto.response.ProjectSummaryResponse;
import com.creatorfund.model.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface ProjectMapper {
    ProjectSummaryResponse toSummaryResponse(Project project);

    ProjectDetailsResponse toDetailsResponse(Project project);

    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "currentAmount", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "createdAt", expression = "java(java.time.ZonedDateTime.now())")
    void updateEntity(@MappingTarget Project project, UpdateProjectRequest request);
}

