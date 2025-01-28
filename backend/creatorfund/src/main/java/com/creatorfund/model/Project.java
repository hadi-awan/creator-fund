package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProjectCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "funding_goal", nullable = false)
    private BigDecimal fundingGoal;

    @Column(name = "current_amount")
    private BigDecimal currentAmount;

    @Column(name = "start_date", nullable = false)
    private ZonedDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private ZonedDateTime endDate;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "risk_assessment")
    private String riskAssessment;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "project")
    private List<ProjectUpdate> updates;

    @OneToMany(mappedBy = "project")
    private List<RewardTier> rewardTiers;
}
