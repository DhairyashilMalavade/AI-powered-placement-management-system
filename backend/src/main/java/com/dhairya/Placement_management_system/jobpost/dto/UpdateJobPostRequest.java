package com.dhairya.Placement_management_system.jobpost.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateJobPostRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 10000)
    private String description;

    @Size(max = 255)
    private String location;

    @Size(max = 100)
    private String salaryRange;

    @Min(1)
    private Integer vacancies;
}
