package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreatePledgeRequest;
import com.creatorfund.dto.response.PledgeResponse;
import com.creatorfund.model.Pledge;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PledgeMapper {
    PledgeResponse toResponse(Pledge pledge);

    @Mapping(target = "status", constant = "PENDING")
    Pledge toEntity(CreatePledgeRequest request);
}
