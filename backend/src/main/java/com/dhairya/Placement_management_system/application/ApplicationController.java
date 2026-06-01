package com.dhairya.Placement_management_system.application;

import com.dhairya.Placement_management_system.application.dto.ApplicationResponse;
import com.dhairya.Placement_management_system.application.dto.CreateApplicationRequest;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request,
                                                    Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.created(applicationService.create(request, userId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<ApplicationResponse>> getMy(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getMyApplications(userId));
    }

    @GetMapping("/job-post/{jobPostId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<List<ApplicationResponse>> getByJobPost(@PathVariable UUID jobPostId,
                                                                Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getApplicationsForJobPost(jobPostId, userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<ApplicationResponse> updateStatus(@PathVariable UUID id,
                                                          @RequestBody Map<String, String> body,
                                                          Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.updateStatus(id, body.get("status"), userId));
    }
}
