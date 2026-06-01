package com.dhairya.Placement_management_system.drive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriveRepository extends JpaRepository<Drive, UUID> {

    List<Drive> findAllByOrderByCreatedAtDesc();

    List<Drive> findByCreatedById(UUID createdById);
}
