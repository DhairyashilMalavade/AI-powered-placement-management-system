package com.dhairya.Placement_management_system.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePlacementOfficerProfileRequest {

    @NotBlank
    private String collegeName;

    private String department;
}
