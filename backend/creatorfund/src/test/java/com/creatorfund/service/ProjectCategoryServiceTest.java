package com.creatorfund.service;

import com.creatorfund.config.BaseServiceTest;
import com.creatorfund.dto.request.CreateProjectCategoryRequest;
import com.creatorfund.dto.response.CategoryResponse;
import com.creatorfund.exception.BusinessValidationException;
import com.creatorfund.mapper.ProjectCategoryMapper;
import com.creatorfund.model.ProjectCategory;
import com.creatorfund.repository.ProjectCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectCategoryServiceTest extends BaseServiceTest {

    @Mock
    private ProjectCategoryRepository categoryRepository;

    @Mock
    private ProjectCategoryMapper categoryMapper;

    private ProjectCategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new ProjectCategoryService(
                categoryRepository,
                categoryMapper
        );
    }

    @Test
    void createCategory_Success() {
        // Arrange
        CreateProjectCategoryRequest request = createSampleCategoryRequest();
        ProjectCategory category = createSampleCategory(null);
        CategoryResponse expectedResponse = createSampleCategoryResponse(null);

        when(categoryRepository.existsByNameIgnoreCase(request.getName())).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(any(ProjectCategory.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        // Act
        CategoryResponse result = categoryService.createCategory(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(expectedResponse.getName());
        verify(categoryRepository).save(any(ProjectCategory.class));
    }

    @Test
    void createCategory_WithParent_Success() {
        // Arrange
        UUID parentId = UUID.randomUUID();
        CreateProjectCategoryRequest request = createSampleCategoryRequestWithParent(parentId);
        ProjectCategory parentCategory = createSampleCategory(null);
        parentCategory.setId(parentId);

        ProjectCategory category = createSampleCategory(parentCategory);
        CategoryResponse expectedResponse = createSampleCategoryResponse(parentId);

        when(categoryRepository.existsByNameIgnoreCase(request.getName())).thenReturn(false);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(any(ProjectCategory.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        // Act
        CategoryResponse result = categoryService.createCategory(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getParentCategoryId()).isEqualTo(parentId);
    }

    @Test
    void createCategory_NameAlreadyExists() {
        // Arrange
        CreateProjectCategoryRequest request = createSampleCategoryRequest();
        when(categoryRepository.existsByNameIgnoreCase(request.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Category with this name already exists");
    }

    @Test
    void getCategory_Success() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        ProjectCategory category = createSampleCategory(null);
        CategoryResponse expectedResponse = createSampleCategoryResponse(null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        // Act
        CategoryResponse result = categoryService.getCategory(categoryId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedResponse.getId());
    }

    @Test
    void getRootCategories_Success() {
        // Arrange
        List<ProjectCategory> categories = Arrays.asList(
                createSampleCategory(null),
                createSampleCategory(null)
        );
        List<CategoryResponse> expectedResponses = Arrays.asList(
                createSampleCategoryResponse(null),
                createSampleCategoryResponse(null)
        );

        when(categoryRepository.findByParentCategoryIdIsNull()).thenReturn(categories);
        when(categoryMapper.toResponse(any(ProjectCategory.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        // Act
        List<CategoryResponse> result = categoryService.getRootCategories();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        ProjectCategory category = createSampleCategory(null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCategoryId(categoryId)).thenReturn(Arrays.asList());

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_WithSubcategories() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        ProjectCategory category = createSampleCategory(null);
        ProjectCategory subcategory = createSampleCategory(category);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCategoryId(categoryId)).thenReturn(Arrays.asList(subcategory));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Cannot delete category with subcategories");
    }

    // Helper methods
    private CreateProjectCategoryRequest createSampleCategoryRequest() {
        CreateProjectCategoryRequest request = new CreateProjectCategoryRequest();
        request.setName("Test Category");
        request.setDescription("Test Description");
        return request;
    }

    private CreateProjectCategoryRequest createSampleCategoryRequestWithParent(UUID parentId) {
        CreateProjectCategoryRequest request = createSampleCategoryRequest();
        request.setParentCategoryId(parentId);
        return request;
    }

    private ProjectCategory createSampleCategory(ProjectCategory parent) {
        ProjectCategory category = new ProjectCategory();
        category.setId(UUID.randomUUID());
        category.setName("Test Category");
        category.setDescription("Test Description");
        category.setParentCategory(parent);
        return category;
    }

    private CategoryResponse createSampleCategoryResponse(UUID parentCategoryId) {
        return CategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Category")
                .description("Test Description")
                .parentCategoryId(parentCategoryId)
                .build();
    }
}