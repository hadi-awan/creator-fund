package com.creatorfund.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(columnDefinition = "jsonb")
    private String socialLinks;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
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

    // Helper methods for managing the relationships
    public void addSkill(String skill) {
        UserSkill userSkill = new UserSkill();
        userSkill.setSkill(skill);
        userSkill.setUserProfile(this);
        skills.add(userSkill);
    }

    public void removeSkill(UserSkill skill) {
        skills.remove(skill);
        skill.setUserProfile(null);
    }

    public void addInterest(String interest) {
        UserInterest userInterest = new UserInterest();
        userInterest.setInterest(interest);
        userInterest.setUserProfile(this);
        interests.add(userInterest);
    }

    public void removeInterest(UserInterest interest) {
        interests.remove(interest);
        interest.setUserProfile(null);
    }

}
