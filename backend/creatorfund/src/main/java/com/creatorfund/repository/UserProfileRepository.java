package com.creatorfund.repository;

import com.creatorfund.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);

    @Query("SELECT DISTINCT up FROM UserProfile up JOIN up.skills s WHERE s.skill = :skillName")
    List<UserProfile> findBySkill(@Param("skillName") String skillName);

    @Query("SELECT DISTINCT up FROM UserProfile up JOIN up.interests i WHERE i.interest = :interestName")
    List<UserProfile> findByInterest(@Param("interestName") String interestName);

    @Query("SELECT up FROM UserProfile up JOIN up.skills s WHERE s.skill = :skill")
    List<UserProfile> findBySkills_Skill(@Param("skill") String skill);

    @Query("SELECT up FROM UserProfile up JOIN up.interests i WHERE i.interest = :interest")
    List<UserProfile> findByInterests_Interest(@Param("interest") String interest);

    // Additional methods for search and filtering
    @Query("SELECT DISTINCT up FROM UserProfile up " +
            "LEFT JOIN up.skills s " +
            "LEFT JOIN up.interests i " +
            "WHERE LOWER(up.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.skill) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(i.interest) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<UserProfile> searchProfiles(@Param("query") String query);
}
