package com.dhairya.Placement_management_system.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OverviewDTO {
    private long totalDrives;
    private long totalJobPosts;
    private long totalApplications;
    private long totalPlacements;
    private Double averageScore;
}
