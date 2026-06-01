package com.dhairya.Placement_management_system.jobpost.dto;

import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class JobPostResponse {

    private UUID id;
    private DriveResponse drive;
    private UserResponse recruiter;
    private String title;
    private String description;
    private String location;
    private String salaryRange;
    private Integer vacancies;
    private String status;
}
