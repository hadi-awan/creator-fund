package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "pledges")
public class Pledge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "backer_id", nullable = false)
    private User backer;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "reward_tier_id")
    private RewardTier rewardTier;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PledgeStatus status;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    private boolean anonymous;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
