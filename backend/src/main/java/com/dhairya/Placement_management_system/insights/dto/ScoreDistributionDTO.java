package com.dhairya.Placement_management_system.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScoreDistributionDTO {
    private String bucket;
    private long count;
}
