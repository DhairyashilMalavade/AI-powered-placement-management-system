package com.dhairya.Placement_management_system.drive;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriveRepository extends JpaRepository<Drive, UUID> {

    @Query(value = "SELECT d FROM Drive d JOIN FETCH d.createdBy",
           countQuery = "SELECT COUNT(d) FROM Drive d")
    Page<Drive> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT d FROM Drive d JOIN FETCH d.createdBy WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) AND d.status = :status",
           countQuery = "SELECT COUNT(d) FROM Drive d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) AND d.status = :status")
    Page<Drive> searchByTitleAndStatus(String search, String status, Pageable pageable);

    @Query(value = "SELECT d FROM Drive d JOIN FETCH d.createdBy WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(d) FROM Drive d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Drive> searchByTitle(String search, Pageable pageable);

    @Query(value = "SELECT d FROM Drive d JOIN FETCH d.createdBy WHERE d.status = :status",
           countQuery = "SELECT COUNT(d) FROM Drive d WHERE d.status = :status")
    Page<Drive> findByStatus(String status, Pageable pageable);

    @Query("SELECT d FROM Drive d JOIN FETCH d.createdBy WHERE d.id = :id")
    Optional<Drive> findByIdWithCreator(UUID id);

    List<Drive> findByCreatedById(UUID createdById);

    long countByStatus(String status);

    @Query(value = "SELECT d.id, d.title, " +
           "COUNT(DISTINCT jp.id) AS total_posts, " +
           "COUNT(a.id) AS total_applicants, " +
           "COUNT(a.id) FILTER (WHERE a.status = 'ACCEPTED') AS total_filled, " +
           "AVG(a.ai_score) FILTER (WHERE a.ai_score IS NOT NULL) AS avg_score " +
           "FROM drives d " +
           "LEFT JOIN job_posts jp ON jp.drive_id = d.id " +
           "LEFT JOIN applications a ON a.job_post_id = jp.id " +
           "GROUP BY d.id, d.title",
           nativeQuery = true)
    List<Object[]> getDrivePerformance();
}
