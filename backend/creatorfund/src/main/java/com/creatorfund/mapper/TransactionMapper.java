package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateTransactionRequest;
import com.creatorfund.dto.response.TransactionResponse;
import com.creatorfund.model.Transaction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "projectId", source = "pledge.project.id")
    @Mapping(target = "projectTitle", source = "pledge.project.title")
    @Mapping(target = "backerId", source = "pledge.backer.id")
    @Mapping(target = "backerName", source = "pledge.backer.fullName")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "status", constant = "PENDING")
    Transaction toEntity(CreateTransactionRequest request);
}
