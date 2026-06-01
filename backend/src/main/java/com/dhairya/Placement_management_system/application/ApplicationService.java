package com.dhairya.Placement_management_system.application;

import com.dhairya.Placement_management_system.application.dto.ApplicationResponse;
import com.dhairya.Placement_management_system.application.dto.CreateApplicationRequest;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.jobpost.JobPost;
import com.dhairya.Placement_management_system.jobpost.JobPostRepository;
import com.dhairya.Placement_management_system.jobpost.dto.JobPostResponse;
import com.dhairya.Placement_management_system.notification.NotificationService;
import com.dhairya.Placement_management_system.user.StudentProfile;
import com.dhairya.Placement_management_system.user.StudentProfileRepository;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final List<String> VALID_STATUSES = List.of(
        "APPLIED", "UNDER_REVIEW", "SHORTLISTED", "ACCEPTED", "REJECTED", "WITHDRAWN");

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final DriveRepository driveRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobPostRepository jobPostRepository,
                              UserRepository userRepository,
                              StudentProfileRepository studentProfileRepository,
                              DriveRepository driveRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobPostRepository = jobPostRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.driveRepository = driveRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request, UUID studentId) {
        UUID jobPostId = UUID.fromString(request.getJobPostId());
        JobPost jobPost = jobPostRepository.findById(jobPostId)
            .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));

        if (!"OPEN".equals(jobPost.getStatus())) {
            throw new BusinessException("Job post is not open for applications");
        }

        Drive drive = jobPost.getDrive();
        if (!"ACTIVE".equals(drive.getStatus())) {
            throw new BusinessException("Drive is not active");
        }

        if (applicationRepository.existsByStudentIdAndJobPostId(studentId, jobPostId)) {
            throw new BusinessException("You have already applied to this job");
        }

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        StudentProfile profile = studentProfileRepository.findByUserId(studentId).orElse(null);

        Application application = new Application();
        application.setStudent(student);
        application.setJobPost(jobPost);
        application.setStatus("APPLIED");
        if (profile != null && profile.getResumeFilePath() != null) {
            application.setResumeSnapshotPath(profile.getResumeFilePath());
        }
        application = applicationRepository.save(application);

        // Notify the recruiter
        User recruiter = jobPost.getRecruiter();
        notificationService.create(
            recruiter.getId(),
            "New Application Received",
            student.getFullName() + " has applied to \"" + jobPost.getTitle() + "\".");

        return toResponse(application);
    }

    public List<ApplicationResponse> getMyApplications(UUID studentId) {
        return applicationRepository.findByStudentIdOrderByAppliedAtDesc(studentId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJobPost(UUID jobPostId, UUID currentUserId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
            .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));

        if (!jobPost.getRecruiter().getId().equals(currentUserId)) {
            throw new BusinessException("You are not the owner of this job post");
        }

        return applicationRepository.findByJobPostIdOrderByAppliedAtDesc(jobPostId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForDrive(UUID driveId, UUID currentUserId) {
        Drive drive = driveRepository.findById(driveId)
            .orElseThrow(() -> new ResourceNotFoundException("Drive", "id", driveId));

        if (!drive.getCreatedBy().getId().equals(currentUserId)) {
            throw new BusinessException("You are not the owner of this drive");
        }

        return applicationRepository.findByJobPostDriveIdOrderByAppliedAtDesc(driveId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID applicationId, String status, UUID currentUserId) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("Invalid status. Accepted: " + VALID_STATUSES);
        }

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        JobPost jobPost = application.getJobPost();
        Drive drive = jobPost.getDrive();

        boolean isRecruiter = jobPost.getRecruiter().getId().equals(currentUserId);
        boolean isPO = drive.getCreatedBy().getId().equals(currentUserId);

        if (!isRecruiter && !isPO) {
            throw new BusinessException("You are not authorized to update this application");
        }

        application.setStatus(status);
        application = applicationRepository.save(application);

        // Notify the student
        User student = application.getStudent();
        notificationService.create(
            student.getId(),
            "Application Status Updated",
            "Your application for \"" + jobPost.getTitle() + "\" is now " + status + ".");

        return toResponse(application);
    }

    private ApplicationResponse toResponse(Application application) {
        JobPost jobPost = application.getJobPost();
        User recruiter = jobPost.getRecruiter();
        Drive drive = jobPost.getDrive();
        User driveCreator = drive.getCreatedBy();

        DriveResponse driveResp = new DriveResponse(
            drive.getId(), drive.getTitle(), drive.getDescription(),
            drive.getMinGpa(), drive.getAllowedGraduationYears(),
            drive.getRequiredSkills(), drive.getAdditionalCriteria(),
            drive.getApplicationDeadline(), drive.getDriveDate(),
            drive.getStatus(),
            new UserResponse(driveCreator.getId(), driveCreator.getEmail(), driveCreator.getFullName(), driveCreator.getRole()),
            drive.getCreatedAt(), drive.getUpdatedAt());

        UserResponse recruiterResp = new UserResponse(
            recruiter.getId(), recruiter.getEmail(), recruiter.getFullName(), recruiter.getRole());

        JobPostResponse jobPostResp = new JobPostResponse(
            jobPost.getId(), driveResp, recruiterResp,
            jobPost.getTitle(), jobPost.getDescription(), jobPost.getLocation(),
            jobPost.getSalaryRange(), jobPost.getVacancies(), jobPost.getStatus());

        UserResponse studentResp = new UserResponse(
            application.getStudent().getId(), application.getStudent().getEmail(),
            application.getStudent().getFullName(), application.getStudent().getRole());

        return new ApplicationResponse(
            application.getId(), studentResp, jobPostResp,
            application.getStatus(), application.getAiScore(),
            application.getResumeSnapshotPath(), application.getAppliedAt(), application.getUpdatedAt());
    }
}
