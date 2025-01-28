package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateNotificationRequest;
import com.creatorfund.dto.response.NotificationResponse;
import com.creatorfund.model.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "readStatus", constant = "false")
    Notification toEntity(CreateNotificationRequest request);
}
