package com.dhairya.Placement_management_system.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    java.util.Optional<StudentProfile> findByUserId(UUID userId);
}
