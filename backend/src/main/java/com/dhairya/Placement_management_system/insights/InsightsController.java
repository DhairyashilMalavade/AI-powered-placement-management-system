package com.dhairya.Placement_management_system.insights;

import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.insights.dto.JobPostOverviewDTO;
import com.dhairya.Placement_management_system.insights.dto.SkillGapDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/skill-gaps")
    @PreAuthorize("hasAnyRole('RECRUITER', 'PO', 'ADMIN')")
    public ApiResponse<List<SkillGapDTO>> getSkillGaps(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(insightsService.getSkillGaps(userId));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('RECRUITER', 'PO', 'ADMIN')")
    public ApiResponse<JobPostOverviewDTO> getOverview(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(insightsService.getOverview(userId));
    }
}
