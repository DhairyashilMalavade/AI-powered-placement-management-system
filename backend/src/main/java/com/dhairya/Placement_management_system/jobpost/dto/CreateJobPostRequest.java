package com.dhairya.Placement_management_system.jobpost.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateJobPostRequest {

    @NotNull
    private UUID driveId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String description;

    @Size(max = 255)
    private String location;

    @Size(max = 100)
    private String salaryRange;

    @NotNull
    @Min(1)
    private Integer vacancies;
}
