package com.dhairya.Placement_management_system.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlacementOfficerProfileRepository extends JpaRepository<PlacementOfficerProfile, UUID> {

    java.util.Optional<PlacementOfficerProfile> findByUserId(UUID userId);
}
