package com.dhairya.Placement_management_system.application;

import com.dhairya.Placement_management_system.application.dto.ApplicationResponse;
import com.dhairya.Placement_management_system.application.dto.CreateApplicationRequest;
import com.dhairya.Placement_management_system.application.dto.ScoredApplicationResponse;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ApplicationResponse> getById(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getById(id, userId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<PagedResponse<ApplicationResponse>> getMy(Authentication auth,
                                                                  @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getMyApplications(userId, pageable));
    }

    @GetMapping("/drive/{driveId}")
    @PreAuthorize("hasRole('PO')")
    public ApiResponse<PagedResponse<ApplicationResponse>> getByDrive(@PathVariable UUID driveId,
                                                                       Authentication auth,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getApplicationsForDrive(driveId, userId, pageable));
    }

    @GetMapping("/job-post/{jobPostId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<PagedResponse<ApplicationResponse>> getByJobPost(@PathVariable UUID jobPostId,
                                                                         Authentication auth,
                                                                         @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getApplicationsForJobPost(jobPostId, userId, pageable));
    }

    @GetMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadResume(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        Resource resource = applicationService.downloadResume(id, userId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<ApplicationResponse> withdraw(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.withdraw(id, userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'PO')")
    public ApiResponse<ApplicationResponse> updateStatus(@PathVariable UUID id,
                                                           @RequestBody Map<String, String> body,
                                                           Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.updateStatus(id, body.get("status"), userId));
    }

    @GetMapping("/job-post/{jobPostId}/ranked")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PagedResponse<ScoredApplicationResponse>> getRanked(
            @PathVariable UUID jobPostId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(applicationService.getRankedApplications(jobPostId, userId, pageable));
    }
}
