package com.dhairya.Placement_management_system.admin.dto;

public record SystemStatsResponse(
    long totalUsers,
    long totalStudents,
    long totalRecruiters,
    long totalPOs,
    long totalAdmins,
    long activeDrives,
    long totalJobPosts,
    long totalApplications,
    long appliedApplications,
    long acceptedApplications,
    long rejectedApplications,
    long withdrawnApplications
) {}
