package com.dhairya.Placement_management_system.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScoringResult {
    private int score;
    private String rationale;
    private String feedback;
    private String version;
}
