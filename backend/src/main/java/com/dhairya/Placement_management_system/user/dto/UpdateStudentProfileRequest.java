package com.dhairya.Placement_management_system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateStudentProfileRequest {

    @NotBlank
    private String collegeName;

    @NotNull
    private Integer graduationYear;

    @NotBlank
    private String major;

    private BigDecimal gpa;
    private String[] skills;
    private String phone;
}
