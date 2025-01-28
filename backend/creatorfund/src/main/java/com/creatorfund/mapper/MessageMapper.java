package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateMessageRequest;
import com.creatorfund.dto.response.MessageResponse;
import com.creatorfund.model.Message;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "senderName", source = "sender.fullName")
    @Mapping(target = "senderProfileImage", source = "sender.profileImageUrl")
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "projectTitle", source = "project.title")
    MessageResponse toResponse(Message message);

    @Mapping(target = "readStatus", constant = "false")
    Message toEntity(CreateMessageRequest request);
}
