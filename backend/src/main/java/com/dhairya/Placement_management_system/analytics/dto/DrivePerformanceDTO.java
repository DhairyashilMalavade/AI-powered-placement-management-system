package com.dhairya.Placement_management_system.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class DrivePerformanceDTO {
    private UUID driveId;
    private String title;
    private long totalPosts;
    private long totalApplicants;
    private long totalFilled;
    private Double averageScore;
}
