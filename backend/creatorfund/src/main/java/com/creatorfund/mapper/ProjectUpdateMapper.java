package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateProjectUpdateRequest;
import com.creatorfund.dto.response.ProjectUpdateResponse;
import com.creatorfund.model.ProjectUpdate;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProjectUpdateMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updateType", ignore = true)
    ProjectUpdate toEntity(CreateProjectUpdateRequest request);

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updateType", expression = "java(update.getUpdateType().name())")
    ProjectUpdateResponse toResponse(ProjectUpdate update);
}
