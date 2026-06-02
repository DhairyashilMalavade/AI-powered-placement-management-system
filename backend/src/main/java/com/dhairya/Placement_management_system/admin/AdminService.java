package com.dhairya.Placement_management_system.admin;

import com.dhairya.Placement_management_system.admin.dto.SystemStatsResponse;
import com.dhairya.Placement_management_system.application.ApplicationRepository;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.jobpost.JobPostRepository;
import com.dhairya.Placement_management_system.user.*;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DriveRepository driveRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PlacementOfficerProfileRepository placementOfficerProfileRepository;

    public AdminService(UserRepository userRepository,
                        DriveRepository driveRepository,
                        JobPostRepository jobPostRepository,
                        ApplicationRepository applicationRepository,
                        StudentProfileRepository studentProfileRepository,
                        RecruiterProfileRepository recruiterProfileRepository,
                        PlacementOfficerProfileRepository placementOfficerProfileRepository) {
        this.userRepository = userRepository;
        this.driveRepository = driveRepository;
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.placementOfficerProfileRepository = placementOfficerProfileRepository;
    }

    public PagedResponse<UserResponse> listUsers(String role, String search, Pageable pageable) {
        Page<User> page;
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasRole = role != null && !role.isBlank();
        if (hasSearch && hasRole) {
            page = userRepository.searchByFullNameEmailAndRole(search, role, pageable);
        } else if (hasSearch) {
            page = userRepository.searchByFullNameOrEmail(search, pageable);
        } else if (hasRole) {
            page = userRepository.findByRole(role, pageable);
        } else {
            page = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PagedResponse.from(page.map(this::toUserResponse));
    }

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(UUID id, String newRole, UUID currentUserId) {
        if (!java.util.List.of("STUDENT", "RECRUITER", "PO").contains(newRole)) {
            throw new BusinessException("Invalid role. Accepted: STUDENT, RECRUITER, PO");
        }

        if (id.equals(currentUserId)) {
            throw new BusinessException("You cannot change your own role");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setRole(newRole);
        user = userRepository.save(user);

        switch (newRole) {
            case "STUDENT" -> {
                if (studentProfileRepository.findByUserId(id).isEmpty()) {
                    StudentProfile profile = new StudentProfile();
                    profile.setUser(user);
                    profile.setCollegeName("Pending");
                    profile.setGraduationYear(LocalDate.now().getYear());
                    profile.setMajor("Pending");
                    studentProfileRepository.save(profile);
                }
            }
            case "RECRUITER" -> {
                if (recruiterProfileRepository.findByUserId(id).isEmpty()) {
                    RecruiterProfile profile = new RecruiterProfile();
                    profile.setUser(user);
                    profile.setCompanyName("Pending");
                    recruiterProfileRepository.save(profile);
                }
            }
            case "PO" -> {
                if (placementOfficerProfileRepository.findByUserId(id).isEmpty()) {
                    PlacementOfficerProfile profile = new PlacementOfficerProfile();
                    profile.setUser(user);
                    profile.setCollegeName("Pending");
                    placementOfficerProfileRepository.save(profile);
                }
            }
        }

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse toggleUserActive(UUID id, UUID currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("You cannot deactivate your own account");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(!user.isActive());
        user = userRepository.save(user);
        return toUserResponse(user);
    }

    public SystemStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRole("STUDENT");
        long totalRecruiters = userRepository.countByRole("RECRUITER");
        long totalPOs = userRepository.countByRole("PO");
        long totalAdmins = userRepository.countByRole("ADMIN");
        long activeDrives = driveRepository.countByStatus("ACTIVE");
        long totalJobPosts = jobPostRepository.count();

        return new SystemStatsResponse(
            totalUsers,
            totalStudents,
            totalRecruiters,
            totalPOs,
            totalAdmins,
            activeDrives,
            totalJobPosts,
            applicationRepository.count(),
            applicationRepository.countByStatus("APPLIED"),
            applicationRepository.countByStatus("ACCEPTED"),
            applicationRepository.countByStatus("REJECTED"),
            applicationRepository.countByStatus("WITHDRAWN")
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive());
    }
}
