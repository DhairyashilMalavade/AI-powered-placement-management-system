package com.dhairya.Placement_management_system.analytics;

import com.dhairya.Placement_management_system.analytics.dto.DrivePerformanceDTO;
import com.dhairya.Placement_management_system.analytics.dto.FunnelDTO;
import com.dhairya.Placement_management_system.analytics.dto.OverviewDTO;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'PO')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewDTO> getOverview() {
        return ApiResponse.success(analyticsService.getOverview());
    }

    @GetMapping("/drive-performance")
    public ApiResponse<List<DrivePerformanceDTO>> getDrivePerformance() {
        return ApiResponse.success(analyticsService.getDrivePerformance());
    }

    @GetMapping("/application-funnel")
    public ApiResponse<FunnelDTO> getApplicationFunnel() {
        return ApiResponse.success(analyticsService.getApplicationFunnel());
    }
}
