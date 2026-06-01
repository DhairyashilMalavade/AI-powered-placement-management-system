package com.dhairya.Placement_management_system.drive.dto;

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
public class DriveResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal minGpa;
    private Integer[] allowedGraduationYears;
    private String[] requiredSkills;
    private String additionalCriteria;
    private LocalDateTime applicationDeadline;
    private LocalDateTime driveDate;
    private String status;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
