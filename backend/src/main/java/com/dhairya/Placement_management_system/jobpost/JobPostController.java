package com.dhairya.Placement_management_system.jobpost;

import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.jobpost.dto.CreateJobPostRequest;
import com.dhairya.Placement_management_system.jobpost.dto.JobPostResponse;
import com.dhairya.Placement_management_system.jobpost.dto.UpdateJobPostRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-posts")
public class JobPostController {

    private final JobPostService jobPostService;

    public JobPostController(JobPostService jobPostService) {
        this.jobPostService = jobPostService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobPostResponse> create(@Valid @RequestBody CreateJobPostRequest request, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.created(jobPostService.create(request, userId));
    }

    @GetMapping("/drive/{driveId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PagedResponse<JobPostResponse>> getByDrive(@PathVariable UUID driveId,
                                                                    @RequestParam(required = false) String search,
                                                                    @RequestParam(required = false) String status,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(jobPostService.getByDrive(driveId, search, status, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<PagedResponse<JobPostResponse>> getMy(Authentication auth,
                                                               @RequestParam(required = false) String search,
                                                               @RequestParam(required = false) String status,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(jobPostService.getMyJobPosts(userId, search, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<JobPostResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(jobPostService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobPostResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateJobPostRequest request,
                                               Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(jobPostService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RECRUITER')")
    public void delete(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        jobPostService.delete(id, userId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobPostResponse> updateStatus(@PathVariable UUID id,
                                                     @RequestBody Map<String, String> body,
                                                     Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(jobPostService.updateStatus(id, body.get("status"), userId));
    }
}
