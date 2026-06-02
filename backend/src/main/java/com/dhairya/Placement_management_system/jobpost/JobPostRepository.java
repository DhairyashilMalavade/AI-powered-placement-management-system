package com.dhairya.Placement_management_system.jobpost;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, UUID> {

    @Query(value = "SELECT jp FROM JobPost jp JOIN FETCH jp.drive d JOIN FETCH d.createdBy JOIN FETCH jp.recruiter WHERE jp.drive.id = :driveId",
           countQuery = "SELECT COUNT(jp) FROM JobPost jp WHERE jp.drive.id = :driveId")
    Page<JobPost> findByDriveIdOrderByTitleAsc(UUID driveId, Pageable pageable);

    @Query(value = "SELECT jp FROM JobPost jp JOIN FETCH jp.drive d JOIN FETCH d.createdBy JOIN FETCH jp.recruiter WHERE jp.recruiter.id = :recruiterId",
           countQuery = "SELECT COUNT(jp) FROM JobPost jp WHERE jp.recruiter.id = :recruiterId")
    Page<JobPost> findByRecruiterId(UUID recruiterId, Pageable pageable);

    @Query(value = "SELECT jp FROM JobPost jp JOIN FETCH jp.drive d JOIN FETCH d.createdBy JOIN FETCH jp.recruiter WHERE jp.drive.id = :driveId AND (:search IS NULL OR LOWER(jp.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(COALESCE(jp.location, '')) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:status IS NULL OR jp.status = :status)",
           countQuery = "SELECT COUNT(jp) FROM JobPost jp WHERE jp.drive.id = :driveId AND (:search IS NULL OR LOWER(jp.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(COALESCE(jp.location, '')) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:status IS NULL OR jp.status = :status)")
    Page<JobPost> searchByDriveId(UUID driveId, String search, String status, Pageable pageable);

    @Query(value = "SELECT jp FROM JobPost jp JOIN FETCH jp.drive d JOIN FETCH d.createdBy JOIN FETCH jp.recruiter WHERE jp.recruiter.id = :recruiterId AND (:search IS NULL OR LOWER(jp.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(COALESCE(jp.location, '')) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:status IS NULL OR jp.status = :status)",
           countQuery = "SELECT COUNT(jp) FROM JobPost jp WHERE jp.recruiter.id = :recruiterId AND (:search IS NULL OR LOWER(jp.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(COALESCE(jp.location, '')) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:status IS NULL OR jp.status = :status)")
    Page<JobPost> searchByRecruiterId(UUID recruiterId, String search, String status, Pageable pageable);

    @Query("SELECT jp FROM JobPost jp JOIN FETCH jp.drive d JOIN FETCH d.createdBy JOIN FETCH jp.recruiter WHERE jp.id = :id")
    Optional<JobPost> findByIdWithDetails(UUID id);

    long countByStatus(String status);
}
