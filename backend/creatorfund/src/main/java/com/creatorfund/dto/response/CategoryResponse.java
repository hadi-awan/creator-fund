package com.creatorfund.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID parentCategoryId;
    private List<CategoryResponse> subcategories;
}
