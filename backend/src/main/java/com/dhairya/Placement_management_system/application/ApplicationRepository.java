package com.dhairya.Placement_management_system.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.student JOIN FETCH a.jobPost jp JOIN FETCH jp.recruiter JOIN FETCH jp.drive d JOIN FETCH d.createdBy WHERE a.student.id = :studentId",
           countQuery = "SELECT COUNT(a) FROM Application a WHERE a.student.id = :studentId")
    Page<Application> findByStudentIdOrderByAppliedAtDesc(UUID studentId, Pageable pageable);

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.student JOIN FETCH a.jobPost jp JOIN FETCH jp.recruiter JOIN FETCH jp.drive d JOIN FETCH d.createdBy WHERE a.jobPost.id = :jobPostId",
           countQuery = "SELECT COUNT(a) FROM Application a WHERE a.jobPost.id = :jobPostId")
    Page<Application> findByJobPostIdOrderByAppliedAtDesc(UUID jobPostId, Pageable pageable);

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.student JOIN FETCH a.jobPost jp JOIN FETCH jp.recruiter JOIN FETCH jp.drive d JOIN FETCH d.createdBy WHERE jp.drive.id = :driveId",
           countQuery = "SELECT COUNT(a) FROM Application a WHERE a.jobPost.drive.id = :driveId")
    Page<Application> findByJobPostDriveIdOrderByAppliedAtDesc(UUID driveId, Pageable pageable);

    @Query("SELECT a.resumeSnapshotPath FROM Application a WHERE a.jobPost.drive.id = :driveId AND a.resumeSnapshotPath IS NOT NULL")
    List<String> findResumePathsByDriveId(UUID driveId);

    @Query("SELECT a.resumeSnapshotPath FROM Application a WHERE a.jobPost.id = :jobPostId AND a.resumeSnapshotPath IS NOT NULL")
    List<String> findResumePathsByJobPostId(UUID jobPostId);

    long countByStatus(String status);

    Optional<Application> findByStudentIdAndJobPostId(UUID studentId, UUID jobPostId);

    boolean existsByStudentIdAndJobPostId(UUID studentId, UUID jobPostId);

    long countByJobPostIdAndStatus(UUID jobPostId, String status);
}
