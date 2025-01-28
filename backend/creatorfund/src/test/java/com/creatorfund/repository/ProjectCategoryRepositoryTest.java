package com.creatorfund.repository;

import com.creatorfund.model.ProjectCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjectCategoryRepositoryTest {

    @Autowired
    private ProjectCategoryRepository categoryRepository;

    @Test
    void shouldFindByParentCategory() {
        // Given
        ProjectCategory parentCategory = new ProjectCategory();
        parentCategory.setName("Technology");
        categoryRepository.save(parentCategory);

        ProjectCategory childCategory1 = new ProjectCategory();
        childCategory1.setName("Software");
        childCategory1.setParentCategory(parentCategory);
        categoryRepository.save(childCategory1);

        ProjectCategory childCategory2 = new ProjectCategory();
        childCategory2.setName("Hardware");
        childCategory2.setParentCategory(parentCategory);
        categoryRepository.save(childCategory2);

        // When
        List<ProjectCategory> childCategories = categoryRepository
                .findByParentCategoryId(parentCategory.getId());

        // Then
        assertThat(childCategories).hasSize(2);
        assertThat(childCategories)
                .extracting(ProjectCategory::getName)
                .containsExactlyInAnyOrder("Software", "Hardware");
    }

    @Test
    void shouldFindByNameIgnoreCase() {
        // Given
        ProjectCategory category = new ProjectCategory();
        category.setName("Technology");
        categoryRepository.save(category);

        // When
        Optional<ProjectCategory> foundCategory1 = categoryRepository
                .findByNameIgnoreCase("TECHNOLOGY");
        Optional<ProjectCategory> foundCategory2 = categoryRepository
                .findByNameIgnoreCase("technology");
        Optional<ProjectCategory> notFoundCategory = categoryRepository
                .findByNameIgnoreCase("NonExistent");

        // Then
        assertThat(foundCategory1).isPresent();
        assertThat(foundCategory2).isPresent();
        assertThat(notFoundCategory).isEmpty();
        assertThat(foundCategory1.get().getName()).isEqualTo("Technology");
    }
}
