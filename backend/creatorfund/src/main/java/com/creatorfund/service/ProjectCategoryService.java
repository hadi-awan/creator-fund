package com.creatorfund.service;

import com.creatorfund.dto.request.CreateProjectCategoryRequest;
import com.creatorfund.dto.response.CategoryResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.exception.ResourceNotFoundException;
import com.creatorfund.mapper.ProjectCategoryMapper;
import com.creatorfund.model.ProjectCategory;
import com.creatorfund.repository.ProjectCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCategoryService {
    private final ProjectCategoryRepository categoryRepository;
    private final ProjectCategoryMapper projectCategoryMapper;

    public CategoryResponse createCategory(CreateProjectCategoryRequest request) {
        validateCategoryName(request.getName());

        ProjectCategory parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        ProjectCategory category = projectCategoryMapper.toEntity(request);
        category.setParentCategory(parentCategory);

        ProjectCategory savedCategory = categoryRepository.save(category);
        return projectCategoryMapper.toResponse(savedCategory);
    }

    public CategoryResponse getCategory(UUID categoryId) {
        ProjectCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return projectCategoryMapper.toResponse(category);
    }

    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentCategoryIdIsNull().stream()
                .map(projectCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getSubcategories(UUID parentId) {
        return categoryRepository.findByParentCategoryId(parentId).stream()
                .map(projectCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse updateCategory(UUID categoryId, CreateProjectCategoryRequest request) {
        ProjectCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(request.getName())) {
            validateCategoryName(request.getName());
        }

        ProjectCategory parentCategory = null;
        if (request.getParentCategoryId() != null) {
            validateParentCategory(categoryId, request.getParentCategoryId());
            parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentCategory(parentCategory);

        ProjectCategory updatedCategory = categoryRepository.save(category);
        return projectCategoryMapper.toResponse(updatedCategory);
    }

    public void deleteCategory(UUID categoryId) {
        ProjectCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check if category has subcategories
        List<ProjectCategory> subcategories = categoryRepository.findByParentCategoryId(categoryId);
        if (!subcategories.isEmpty()) {
            throw new BusinessValidationException("Cannot delete category with subcategories");
        }

        // Check if category has projects
        if (hasProjects(category)) {
            throw new BusinessValidationException("Cannot delete category with existing projects");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryResponse> searchCategories(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query).stream()
                .map(projectCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateCategoryName(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessValidationException("Category with this name already exists");
        }
    }

    private void validateParentCategory(UUID categoryId, UUID parentId) {
        if (categoryId.equals(parentId)) {
            throw new BusinessValidationException("Category cannot be its own parent");
        }

        // Check for circular reference
        ProjectCategory parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));

        ProjectCategory current = parent.getParentCategory();
        while (current != null) {
            if (current.getId().equals(categoryId)) {
                throw new BusinessValidationException("Circular reference in category hierarchy");
            }
            current = current.getParentCategory();
        }
    }

    private boolean hasProjects(ProjectCategory category) {
        return category.getProjects() != null && !category.getProjects().isEmpty();
    }
}
