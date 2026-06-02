package com.dhairya.Placement_management_system.jobpost;

import com.dhairya.Placement_management_system.application.ApplicationRepository;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.jobpost.dto.CreateJobPostRequest;
import com.dhairya.Placement_management_system.jobpost.dto.JobPostResponse;
import com.dhairya.Placement_management_system.jobpost.dto.UpdateJobPostRequest;
import com.dhairya.Placement_management_system.resume.FileStorageService;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobPostService {

    private static final List<String> VALID_STATUSES = List.of("OPEN", "FILLED", "CANCELLED");

    private final JobPostRepository jobPostRepository;
    private final DriveRepository driveRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;

    public JobPostService(JobPostRepository jobPostRepository,
                          DriveRepository driveRepository,
                          UserRepository userRepository,
                          ApplicationRepository applicationRepository,
                          FileStorageService fileStorageService) {
        this.jobPostRepository = jobPostRepository;
        this.driveRepository = driveRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public JobPostResponse create(CreateJobPostRequest request, UUID recruiterId) {
        Drive drive = driveRepository.findById(request.getDriveId())
            .orElseThrow(() -> new ResourceNotFoundException("Drive", "id", request.getDriveId()));

        if (!"ACTIVE".equals(drive.getStatus())) {
            throw new BusinessException("Cannot create job post for a non-active drive");
        }

        User recruiter = userRepository.findById(recruiterId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));

        JobPost post = new JobPost();
        post.setDrive(drive);
        post.setRecruiter(recruiter);
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setLocation(request.getLocation());
        post.setSalaryRange(request.getSalaryRange());
        post.setVacancies(request.getVacancies());
        post = jobPostRepository.save(post);
        return toResponse(post);
    }

    public PagedResponse<JobPostResponse> getByDrive(UUID driveId, String search, String status, Pageable pageable) {
        Page<JobPostResponse> page;
        if ((search != null && !search.isBlank()) || (status != null && !status.isBlank())) {
            page = jobPostRepository.searchByDriveId(driveId, search, status, pageable)
                .map(this::toResponse);
        } else {
            page = jobPostRepository.findByDriveIdOrderByTitleAsc(driveId, pageable)
                .map(this::toResponse);
        }
        return PagedResponse.from(page);
    }

    public PagedResponse<JobPostResponse> getMyJobPosts(UUID recruiterId, String search, String status, Pageable pageable) {
        Page<JobPostResponse> page;
        if ((search != null && !search.isBlank()) || (status != null && !status.isBlank())) {
            page = jobPostRepository.searchByRecruiterId(recruiterId, search, status, pageable)
                .map(this::toResponse);
        } else {
            page = jobPostRepository.findByRecruiterId(recruiterId, pageable)
                .map(this::toResponse);
        }
        return PagedResponse.from(page);
    }

    public JobPostResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public JobPostResponse update(UUID id, UpdateJobPostRequest request, UUID currentUserId) {
        JobPost post = findById(id);
        verifyOwner(post, currentUserId);

        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getDescription() != null) post.setDescription(request.getDescription());
        if (request.getLocation() != null) post.setLocation(request.getLocation());
        if (request.getSalaryRange() != null) post.setSalaryRange(request.getSalaryRange());
        if (request.getVacancies() != null) post.setVacancies(request.getVacancies());

        post = jobPostRepository.save(post);
        return toResponse(post);
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        JobPost post = findById(id);
        verifyOwner(post, currentUserId);

        List<String> resumePaths = applicationRepository.findResumePathsByJobPostId(id);
        for (String path : resumePaths) {
            fileStorageService.delete(path);
        }

        jobPostRepository.delete(post);
    }

    @Transactional
    public JobPostResponse updateStatus(UUID id, String status, UUID currentUserId) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("Invalid status. Accepted: " + VALID_STATUSES);
        }
        JobPost post = findById(id);
        verifyOwner(post, currentUserId);
        post.setStatus(status);
        post = jobPostRepository.save(post);
        return toResponse(post);
    }

    private JobPost findById(UUID id) {
        return jobPostRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", id));
    }

    private void verifyOwner(JobPost post, UUID userId) {
        if (!post.getRecruiter().getId().equals(userId)) {
            throw new BusinessException("You are not the owner of this job post");
        }
    }

    private JobPostResponse toResponse(JobPost post) {
        Drive drive = post.getDrive();
        User creator = drive.getCreatedBy();
        DriveResponse driveResp = new DriveResponse(
            drive.getId(), drive.getTitle(), drive.getDescription(),
            drive.getMinGpa(), drive.getAllowedGraduationYears(),
            drive.getRequiredSkills(), drive.getAdditionalCriteria(),
            drive.getApplicationDeadline(), drive.getDriveDate(),
            drive.getStatus(),
            new UserResponse(creator.getId(), creator.getEmail(), creator.getFullName(), creator.getRole(), creator.isActive()),
            drive.getCreatedAt(), drive.getUpdatedAt());

        User recruiter = post.getRecruiter();
        UserResponse recruiterResp = new UserResponse(
            recruiter.getId(), recruiter.getEmail(), recruiter.getFullName(), recruiter.getRole(), recruiter.isActive());

        return new JobPostResponse(post.getId(), driveResp, recruiterResp,
            post.getTitle(), post.getDescription(), post.getLocation(),
            post.getSalaryRange(), post.getVacancies(), post.getStatus());
    }
}
