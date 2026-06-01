package com.dhairya.Placement_management_system.application.dto;

import com.dhairya.Placement_management_system.jobpost.dto.JobPostResponse;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private UserResponse student;
    private JobPostResponse jobPost;
    private String status;
    private BigDecimal aiScore;
    private String resumeSnapshotPath;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
