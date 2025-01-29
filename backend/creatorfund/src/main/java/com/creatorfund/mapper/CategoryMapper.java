package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateProjectCategoryRequest;
import com.creatorfund.dto.response.CategoryResponse;
import com.creatorfund.model.ProjectCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProjectCategory toEntity(CreateProjectCategoryRequest request);

    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    @Mapping(target = "subcategories", ignore = true)
    CategoryResponse toResponse(ProjectCategory category);
}