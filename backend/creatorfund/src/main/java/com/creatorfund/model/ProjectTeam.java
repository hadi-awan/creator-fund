package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "project_teams")
public class ProjectTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")  // Changed from jsonb to json for H2 compatibility
    private String permissions;

    @Column(name = "joined_at", nullable = false)
    private ZonedDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = ZonedDateTime.now();
        }
        if (permissions == null) {
            permissions = "{}";
        }
    }
}