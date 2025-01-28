package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateRefundRequest;
import com.creatorfund.dto.response.RefundResponse;
import com.creatorfund.model.Refund;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RefundMapper {
    @Mapping(target = "projectId", source = "transaction.pledge.project.id")
    @Mapping(target = "projectTitle", source = "transaction.pledge.project.title")
    @Mapping(target = "backerId", source = "transaction.pledge.backer.id")
    @Mapping(target = "backerName", source = "transaction.pledge.backer.fullName")
    RefundResponse toResponse(Refund refund);

    @Mapping(target = "status", constant = "PENDING")
    Refund toEntity(CreateRefundRequest request);
}
