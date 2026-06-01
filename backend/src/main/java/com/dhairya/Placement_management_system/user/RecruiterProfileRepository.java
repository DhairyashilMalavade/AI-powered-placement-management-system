package com.dhairya.Placement_management_system.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, UUID> {

    java.util.Optional<RecruiterProfile> findByUserId(UUID userId);
}
