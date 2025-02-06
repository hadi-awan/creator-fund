package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String location;

    private String website;

    @Column(name = "social_links")
    @JdbcTypeCode(SqlTypes.JSON)
    private String socialLinks;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    private List<UserSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    private List<UserInterest> interests = new ArrayList<>();

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    public void addSkill(String skillName) {
        UserSkill skill = new UserSkill();
        skill.setSkill(skillName);
        skill.setUserProfile(this);
        skills.add(skill);
    }

    public void removeSkill(String skillName) {
        skills.removeIf(skill -> skill.getSkill().equals(skillName));
    }

    public void addInterest(String interestName) {
        UserInterest interest = new UserInterest();
        interest.setInterest(interestName);
        interest.setUserProfile(this);
        interests.add(interest);
    }

    public void removeInterest(String interestName) {
        interests.removeIf(interest -> interest.getInterest().equals(interestName));
    }
}
