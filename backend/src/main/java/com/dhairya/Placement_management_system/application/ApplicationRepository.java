package com.dhairya.Placement_management_system.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByStudentIdOrderByAppliedAtDesc(UUID studentId);

    List<Application> findByJobPostIdOrderByAppliedAtDesc(UUID jobPostId);

    List<Application> findByJobPostDriveIdOrderByAppliedAtDesc(UUID driveId);

    Optional<Application> findByStudentIdAndJobPostId(UUID studentId, UUID jobPostId);

    boolean existsByStudentIdAndJobPostId(UUID studentId, UUID jobPostId);
}
