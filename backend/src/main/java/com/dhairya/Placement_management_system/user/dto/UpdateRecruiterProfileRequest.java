package com.dhairya.Placement_management_system.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRecruiterProfileRequest {

    @NotBlank
    private String companyName;

    private String companyWebsite;
    private String companyDescription;
}
