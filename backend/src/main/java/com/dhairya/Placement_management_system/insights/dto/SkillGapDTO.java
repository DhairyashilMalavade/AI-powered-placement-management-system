package com.dhairya.Placement_management_system.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkillGapDTO {
    private String skill;
    private long requiredCount;
    private long matchedCount;
    private double gapPercentage;
}
