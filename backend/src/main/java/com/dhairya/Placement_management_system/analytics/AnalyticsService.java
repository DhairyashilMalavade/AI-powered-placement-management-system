package com.dhairya.Placement_management_system.analytics;

import com.dhairya.Placement_management_system.analytics.dto.DrivePerformanceDTO;
import com.dhairya.Placement_management_system.analytics.dto.FunnelDTO;
import com.dhairya.Placement_management_system.analytics.dto.OverviewDTO;
import com.dhairya.Placement_management_system.application.Application;
import com.dhairya.Placement_management_system.application.ApplicationRepository;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.jobpost.JobPostRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final DriveRepository driveRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    public AnalyticsService(DriveRepository driveRepository,
                            JobPostRepository jobPostRepository,
                            ApplicationRepository applicationRepository) {
        this.driveRepository = driveRepository;
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
    }

    public OverviewDTO getOverview() {
        long totalDrives = driveRepository.count();
        long totalJobPosts = jobPostRepository.count();
        long totalApplications = applicationRepository.count();
        long totalPlacements = applicationRepository.countByStatus("ACCEPTED");
        Double averageScore = applicationRepository.getAverageAiScore();

        return new OverviewDTO(totalDrives, totalJobPosts, totalApplications, totalPlacements, averageScore);
    }

    public List<DrivePerformanceDTO> getDrivePerformance() {
        List<Object[]> results = driveRepository.getDrivePerformance();
        return results.stream()
            .map(row -> new DrivePerformanceDTO(
                (UUID) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                row[5] != null ? ((Number) row[5]).doubleValue() : null
            ))
            .collect(Collectors.toList());
    }

    public FunnelDTO getApplicationFunnel() {
        List<Object[]> results = applicationRepository.getPlatformFunnel();
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Object[] row : results) {
            statusCounts.put((String) row[0], ((Number) row[1]).longValue());
        }
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        return new FunnelDTO(statusCounts, total);
    }
}
