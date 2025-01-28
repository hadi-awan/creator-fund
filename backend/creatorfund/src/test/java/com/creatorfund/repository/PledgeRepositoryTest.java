package com.creatorfund.repository;

import com.creatorfund.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PledgeRepositoryTest {

    @Autowired
    private PledgeRepository pledgeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    @Test
    void shouldFindPledgesByProjectAndStatus() {
        // Given
        User backer = new User();
        backer.setEmail("backer@example.com");
        backer.setFullName("Backer");
        backer.setPasswordHash("hashedPassword");
        userRepository.save(backer);

        Project project = createTestProject();
        projectRepository.save(project);

        Pledge pledge1 = new Pledge();
        pledge1.setBacker(backer);
        pledge1.setProject(project);
        pledge1.setAmount(new BigDecimal("100.00"));
        pledge1.setStatus(PledgeStatus.SUCCESSFUL);
        pledgeRepository.save(pledge1);

        Pledge pledge2 = new Pledge();
        pledge2.setBacker(backer);
        pledge2.setProject(project);
        pledge2.setAmount(new BigDecimal("200.00"));
        pledge2.setStatus(PledgeStatus.PENDING);
        pledgeRepository.save(pledge2);

        // When
        List<Pledge> successfulPledges = pledgeRepository
                .findByProjectIdAndStatus(project.getId(), PledgeStatus.SUCCESSFUL);

        // Then
        assertThat(successfulPledges).hasSize(1);
        assertThat(successfulPledges.get(0).getAmount())
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    private Project createTestProject() {
        User creator = new User();
        creator.setEmail("creator@example.com");
        creator.setFullName("Creator");
        creator.setPasswordHash("hashedPassword");
        userRepository.save(creator);

        ProjectCategory category = new ProjectCategory();
        category.setName("Test Category");
        projectCategoryRepository.save(category);

        Project project = new Project();
        project.setCreator(creator);
        project.setCategory(category);
        project.setTitle("Test Project");
        project.setDescription("Description");
        project.setFundingGoal(new BigDecimal("1000.00"));
        project.setStartDate(ZonedDateTime.now());
        project.setEndDate(ZonedDateTime.now().plusDays(30));
        return project;
    }
}
