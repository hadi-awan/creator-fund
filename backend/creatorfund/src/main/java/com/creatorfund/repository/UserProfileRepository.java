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
}
