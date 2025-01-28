package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateRewardTierRequest;
import com.creatorfund.dto.response.RewardTierResponse;
import com.creatorfund.model.RewardTier;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RewardTierMapper {
    RewardTierResponse toResponse(RewardTier rewardTier);

    @Mapping(target = "currentBackers", constant = "0")
    RewardTier toEntity(CreateRewardTierRequest request);
}
