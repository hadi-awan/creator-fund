package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateCommentRequest;
import com.creatorfund.dto.response.CommentResponse;
import com.creatorfund.model.Comments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Comments toEntity(CreateCommentRequest request);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "replies", ignore = true)
    CommentResponse toResponse(Comments comments);
}