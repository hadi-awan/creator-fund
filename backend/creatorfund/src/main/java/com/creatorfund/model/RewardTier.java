package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "reward_tiers")
public class RewardTier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "limit_count")
    private Integer limitCount;

    @Column(name = "current_backers")
    private Integer currentBackers = 0;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_type")
    private ShippingType shippingType;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        if (currentBackers == null) {
            currentBackers = 0;
        }
    }
}
