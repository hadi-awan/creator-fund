package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateProjectCategoryRequest;
import com.creatorfund.dto.response.CategoryResponse;
import com.creatorfund.model.ProjectCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(ProjectCategory category);
    ProjectCategory toEntity(CreateProjectCategoryRequest request);
}
