package com.dhairya.Placement_management_system.drive.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDriveRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 10000)
    private String description;

    private BigDecimal minGpa;

    private Integer[] allowedGraduationYears;

    private String[] requiredSkills;

    @Size(max = 10000)
    private String additionalCriteria;

    private LocalDateTime applicationDeadline;

    private LocalDateTime driveDate;
}
