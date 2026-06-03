package com.dhairya.Placement_management_system.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JobPostOverviewDTO {
    private List<ScoreDistributionDTO> scoreDistribution;
    private List<StatusCountDTO> funnel;
}
