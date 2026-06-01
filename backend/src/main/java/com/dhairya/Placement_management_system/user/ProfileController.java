package com.dhairya.Placement_management_system.user;

import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.user.dto.*;
import jakarta.validation.Valid;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.user.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(profileService.getProfile(userId));
    }

    @PutMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<ProfileResponse> updateStudent(@Valid @RequestBody UpdateStudentProfileRequest request,
                                                       Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(profileService.updateStudentProfile(userId, request));
    }

    @PutMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<ProfileResponse> updateRecruiter(@Valid @RequestBody UpdateRecruiterProfileRequest request,
                                                         Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(profileService.updateRecruiterProfile(userId, request));
    }

    @PutMapping("/po")
    @PreAuthorize("hasRole('PO')")
    public ApiResponse<ProfileResponse> updatePO(@Valid @RequestBody UpdatePlacementOfficerProfileRequest request,
                                                  Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(profileService.updatePlacementOfficerProfile(userId, request));
    }
}
