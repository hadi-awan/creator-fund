package com.creatorfund.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NotificationFilterRequest {
    private String type;
    private Boolean readStatus;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Integer page;
    private Integer size;
}