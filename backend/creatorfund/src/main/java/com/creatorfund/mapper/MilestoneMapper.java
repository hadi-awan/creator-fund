package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateMilestoneRequest;
import com.creatorfund.dto.response.MilestoneResponse;
import com.creatorfund.model.Milestone;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {
    MilestoneResponse toResponse(Milestone milestone);

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "completionDate", ignore = true)
    Milestone toEntity(CreateMilestoneRequest request);
}
