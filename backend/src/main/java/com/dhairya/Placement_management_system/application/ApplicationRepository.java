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

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.student " +
           "WHERE a.jobPost.id = :jobPostId AND a.status <> 'WITHDRAWN' " +
           "ORDER BY a.aiScore DESC NULLS LAST",
           countQuery = "SELECT COUNT(a) FROM Application a WHERE a.jobPost.id = :jobPostId AND a.status <> 'WITHDRAWN'")
    Page<Application> findRankedByJobPostId(UUID jobPostId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.id = :jobPostId AND a.aiScore > :aiScore AND a.status <> 'WITHDRAWN'")
    long countByJobPostIdAndAiScoreGreaterThan(UUID jobPostId, java.math.BigDecimal aiScore);

    @Query(value = "SELECT " +
           "CASE " +
            "  WHEN a.aiScore >= 81 THEN '81-100' " +
            "  WHEN a.aiScore >= 61 THEN '61-80' " +
            "  WHEN a.aiScore >= 41 THEN '41-60' " +
            "  WHEN a.aiScore >= 21 THEN '21-40' " +
            "  WHEN a.aiScore IS NOT NULL THEN '0-20' " +
           "  ELSE 'unscored' " +
           "END as bucket, COUNT(a) " +
           "FROM Application a WHERE a.jobPost.id IN :jobPostIds " +
           "GROUP BY bucket ORDER BY bucket")
    List<Object[]> getScoreDistribution(java.util.List<UUID> jobPostIds);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.jobPost.id IN :jobPostIds GROUP BY a.status ORDER BY a.status")
    List<Object[]> getFunnelByJobPostIds(java.util.List<UUID> jobPostIds);

    long countByStatusIn(java.util.List<String> statuses);

    @Query("SELECT AVG(a.aiScore) FROM Application a WHERE a.aiScore IS NOT NULL")
    Double getAverageAiScore();

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status ORDER BY a.status")
    List<Object[]> getPlatformFunnel();
}
