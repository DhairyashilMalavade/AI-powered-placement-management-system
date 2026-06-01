package com.dhairya.Placement_management_system.jobpost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, UUID> {

    List<JobPost> findByDriveIdOrderByTitleAsc(UUID driveId);

    List<JobPost> findByRecruiterId(UUID recruiterId);
}
