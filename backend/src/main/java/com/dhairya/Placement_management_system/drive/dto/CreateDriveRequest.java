package com.dhairya.Placement_management_system.drive.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateDriveRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 10000)
    private String description;

    @DecimalMin("0.00")
    @DecimalMax("10.00")
    private BigDecimal minGpa;

    private Integer[] allowedGraduationYears;

    private String[] requiredSkills;

    @Size(max = 10000)
    private String additionalCriteria;

    @NotNull
    @Future
    private LocalDateTime applicationDeadline;

    @Future
    private LocalDateTime driveDate;
}
