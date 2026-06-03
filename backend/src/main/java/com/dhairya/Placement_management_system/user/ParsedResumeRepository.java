package com.dhairya.Placement_management_system.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParsedResumeRepository extends JpaRepository<ParsedResume, Long> {
    Optional<ParsedResume> findByStudentId(UUID studentId);
}
