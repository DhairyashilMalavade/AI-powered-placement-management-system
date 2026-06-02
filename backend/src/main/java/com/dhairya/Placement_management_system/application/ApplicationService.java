package com.dhairya.Placement_management_system.application;

import com.dhairya.Placement_management_system.application.dto.ApplicationResponse;
import com.dhairya.Placement_management_system.application.dto.CreateApplicationRequest;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.jobpost.JobPost;
import com.dhairya.Placement_management_system.jobpost.JobPostRepository;
import com.dhairya.Placement_management_system.jobpost.dto.JobPostResponse;
import com.dhairya.Placement_management_system.notification.NotificationService;
import com.dhairya.Placement_management_system.resume.FileStorageService;
import com.dhairya.Placement_management_system.user.StudentProfile;
import com.dhairya.Placement_management_system.user.StudentProfileRepository;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ApplicationService {

    private static final java.util.List<String> VALID_STATUSES = java.util.List.of(
        "APPLIED", "UNDER_REVIEW", "SHORTLISTED", "ACCEPTED", "REJECTED", "WITHDRAWN");

    private static final java.util.List<String> UPDATEABLE_STATUSES = java.util.List.of(
        "APPLIED", "UNDER_REVIEW", "SHORTLISTED", "ACCEPTED", "REJECTED");

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final DriveRepository driveRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobPostRepository jobPostRepository,
                              UserRepository userRepository,
                              StudentProfileRepository studentProfileRepository,
                              DriveRepository driveRepository,
                              NotificationService notificationService,
                              FileStorageService fileStorageService) {
        this.applicationRepository = applicationRepository;
        this.jobPostRepository = jobPostRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.driveRepository = driveRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
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

        try {
            application = applicationRepository.save(application);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("You have already applied to this job");
        }

        // Notify the recruiter
        User recruiter = jobPost.getRecruiter();
        notificationService.create(
            recruiter.getId(),
            "New Application Received",
            student.getFullName() + " has applied to \"" + jobPost.getTitle() + "\".",
            "/drives/" + jobPost.getDrive().getId());

        return toResponse(application);
    }

    public PagedResponse<ApplicationResponse> getMyApplications(UUID studentId, Pageable pageable) {
        Page<ApplicationResponse> page = applicationRepository.findByStudentIdOrderByAppliedAtDesc(studentId, pageable)
            .map(this::toResponse);
        return PagedResponse.from(page);
    }

    public PagedResponse<ApplicationResponse> getApplicationsForJobPost(UUID jobPostId, UUID currentUserId, Pageable pageable) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
            .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));

        if (!jobPost.getRecruiter().getId().equals(currentUserId)) {
            throw new BusinessException("You are not the owner of this job post");
        }

        Page<ApplicationResponse> page = applicationRepository.findByJobPostIdOrderByAppliedAtDesc(jobPostId, pageable)
            .map(this::toResponse);
        return PagedResponse.from(page);
    }

    public PagedResponse<ApplicationResponse> getApplicationsForDrive(UUID driveId, UUID currentUserId, Pageable pageable) {
        Drive drive = driveRepository.findById(driveId)
            .orElseThrow(() -> new ResourceNotFoundException("Drive", "id", driveId));

        if (!drive.getCreatedBy().getId().equals(currentUserId)) {
            throw new BusinessException("You are not the owner of this drive");
        }

        Page<ApplicationResponse> page = applicationRepository.findByJobPostDriveIdOrderByAppliedAtDesc(driveId, pageable)
            .map(this::toResponse);
        return PagedResponse.from(page);
    }

    @Transactional
    public ApplicationResponse withdraw(UUID applicationId, UUID studentId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getStudent().getId().equals(studentId)) {
            throw new BusinessException("You can only withdraw your own application");
        }

        if (!"APPLIED".equals(application.getStatus()) && !"UNDER_REVIEW".equals(application.getStatus())
            && !"SHORTLISTED".equals(application.getStatus())) {
            throw new BusinessException("Application cannot be withdrawn in its current state");
        }

        JobPost jobPost = application.getJobPost();

        application.setStatus("WITHDRAWN");
        application = applicationRepository.save(application);

        updateJobPostStatus(jobPost);

        // Notify the recruiter
        User recruiter = jobPost.getRecruiter();
        notificationService.create(
            recruiter.getId(),
            "Application Withdrawn",
            application.getStudent().getFullName() + " has withdrawn their application for \"" + jobPost.getTitle() + "\".",
            "/drives/" + jobPost.getDrive().getId());

        return toResponse(application);
    }

    private static final java.util.Map<String, java.util.Set<String>> ALLOWED_TRANSITIONS = java.util.Map.of(
        "APPLIED", java.util.Set.of("UNDER_REVIEW", "SHORTLISTED", "ACCEPTED", "REJECTED"),
        "UNDER_REVIEW", java.util.Set.of("SHORTLISTED", "ACCEPTED", "REJECTED"),
        "SHORTLISTED", java.util.Set.of("ACCEPTED", "REJECTED"),
        "ACCEPTED", java.util.Set.of("REJECTED"),
        "REJECTED", java.util.Set.of());

    @Transactional
    public ApplicationResponse updateStatus(UUID applicationId, String status, UUID currentUserId) {
        if (!UPDATEABLE_STATUSES.contains(status)) {
            throw new BusinessException("Invalid status. Accepted: " + UPDATEABLE_STATUSES);
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

        String oldStatus = application.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(oldStatus, java.util.Set.of()).contains(status)) {
            throw new BusinessException("Cannot change status from " + oldStatus + " to " + status);
        }

        if ("ACCEPTED".equals(status)) {
            long currentAccepted = applicationRepository.countByJobPostIdAndStatus(jobPost.getId(), "ACCEPTED");
            if (currentAccepted >= jobPost.getVacancies()) {
                throw new BusinessException("All vacancies for this position have been filled");
            }
        }

        application.setStatus(status);
        application = applicationRepository.save(application);

        updateJobPostStatus(jobPost);

        // Notify the student
        User student = application.getStudent();
        notificationService.create(
            student.getId(),
            "Application Status Updated",
            "Your application for \"" + jobPost.getTitle() + "\" is now " + status + ".",
            "/drives/" + jobPost.getDrive().getId());

        return toResponse(application);
    }

    private void updateJobPostStatus(JobPost jobPost) {
        long acceptedCount = applicationRepository.countByJobPostIdAndStatus(jobPost.getId(), "ACCEPTED");
        if (acceptedCount >= jobPost.getVacancies()) {
            if (!"FILLED".equals(jobPost.getStatus())) {
                jobPost.setStatus("FILLED");
                jobPostRepository.save(jobPost);
                User recruiter = jobPost.getRecruiter();
                notificationService.create(
                    recruiter.getId(),
                    "Job Post Filled",
                    "\"" + jobPost.getTitle() + "\" has been closed as all vacancies are filled.",
                    "/drives/" + jobPost.getDrive().getId());
            }
        } else {
            if ("FILLED".equals(jobPost.getStatus())) {
                jobPost.setStatus("OPEN");
                jobPostRepository.save(jobPost);
                User recruiter = jobPost.getRecruiter();
                notificationService.create(
                    recruiter.getId(),
                    "Job Post Reopened",
                    "\"" + jobPost.getTitle() + "\" has been reopened as a vacancy became available.",
                    "/drives/" + jobPost.getDrive().getId());
            }
        }
    }

    public Resource downloadResume(UUID applicationId, UUID currentUserId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (application.getResumeSnapshotPath() == null) {
            throw new BusinessException("No resume attached to this application");
        }

        JobPost jobPost = application.getJobPost();
        Drive drive = jobPost.getDrive();

        boolean isStudent = application.getStudent().getId().equals(currentUserId);
        boolean isRecruiter = jobPost.getRecruiter().getId().equals(currentUserId);
        boolean isPO = drive.getCreatedBy().getId().equals(currentUserId);

        if (!isStudent && !isRecruiter && !isPO) {
            throw new BusinessException("You are not authorized to download this resume");
        }

        Resource resource = fileStorageService.load(application.getResumeSnapshotPath());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Resume", "filename", application.getResumeSnapshotPath());
        }
        return resource;
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
            new UserResponse(driveCreator.getId(), driveCreator.getEmail(), driveCreator.getFullName(), driveCreator.getRole(), driveCreator.isActive()),
            drive.getCreatedAt(), drive.getUpdatedAt());

        UserResponse recruiterResp = new UserResponse(
            recruiter.getId(), recruiter.getEmail(), recruiter.getFullName(), recruiter.getRole(), recruiter.isActive());

        JobPostResponse jobPostResp = new JobPostResponse(
            jobPost.getId(), driveResp, recruiterResp,
            jobPost.getTitle(), jobPost.getDescription(), jobPost.getLocation(),
            jobPost.getSalaryRange(), jobPost.getVacancies(), jobPost.getStatus());

        UserResponse studentResp = new UserResponse(
            application.getStudent().getId(), application.getStudent().getEmail(),
            application.getStudent().getFullName(), application.getStudent().getRole(),
            application.getStudent().isActive());

        return new ApplicationResponse(
            application.getId(), studentResp, jobPostResp,
            application.getStatus(), application.getAiScore(),
            application.getResumeSnapshotPath(), application.getAppliedAt(), application.getUpdatedAt());
    }
}
