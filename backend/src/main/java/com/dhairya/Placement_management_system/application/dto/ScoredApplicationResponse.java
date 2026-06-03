package com.dhairya.Placement_management_system.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoredApplicationResponse {
    private UUID applicationId;
    private String studentName;
    private String studentId;
    private String status;
    private BigDecimal aiScore;
    private String scoringFeedback;
    private Integer rank;

    private String scoringRationale;
    private String scoringVersion;
}
