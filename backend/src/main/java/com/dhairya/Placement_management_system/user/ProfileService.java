package com.dhairya.Placement_management_system.user;

import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.user.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PlacementOfficerProfileRepository placementOfficerProfileRepository;

    public ProfileService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          RecruiterProfileRepository recruiterProfileRepository,
                          PlacementOfficerProfileRepository placementOfficerProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.placementOfficerProfileRepository = placementOfficerProfileRepository;
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return switch (user.getRole()) {
            case "STUDENT" -> {
                StudentProfile p = studentProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));
                yield ProfileResponse.from(p);
            }
            case "RECRUITER" -> {
                RecruiterProfile p = recruiterProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", userId));
                yield ProfileResponse.from(p);
            }
            case "PO" -> {
                PlacementOfficerProfile p = placementOfficerProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("PlacementOfficerProfile", "userId", userId));
                yield ProfileResponse.from(p);
            }
            default -> throw new BusinessException("Profile not available for role: " + user.getRole());
        };
    }

    @Transactional
    public ProfileResponse updateStudentProfile(UUID userId, UpdateStudentProfileRequest request) {
        StudentProfile p = studentProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        p.setCollegeName(request.getCollegeName());
        p.setGraduationYear(request.getGraduationYear());
        p.setMajor(request.getMajor());
        p.setGpa(request.getGpa());
        p.setSkills(request.getSkills());
        p.setPhone(request.getPhone());
        p = studentProfileRepository.save(p);
        return ProfileResponse.from(p);
    }

    @Transactional
    public ProfileResponse updateRecruiterProfile(UUID userId, UpdateRecruiterProfileRequest request) {
        RecruiterProfile p = recruiterProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("RecruiterProfile", "userId", userId));

        p.setCompanyName(request.getCompanyName());
        p.setCompanyWebsite(request.getCompanyWebsite());
        p.setCompanyDescription(request.getCompanyDescription());
        p = recruiterProfileRepository.save(p);
        return ProfileResponse.from(p);
    }

    @Transactional
    public ProfileResponse updatePlacementOfficerProfile(UUID userId, UpdatePlacementOfficerProfileRequest request) {
        PlacementOfficerProfile p = placementOfficerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("PlacementOfficerProfile", "userId", userId));

        p.setCollegeName(request.getCollegeName());
        p.setDepartment(request.getDepartment());
        p = placementOfficerProfileRepository.save(p);
        return ProfileResponse.from(p);
    }
}
