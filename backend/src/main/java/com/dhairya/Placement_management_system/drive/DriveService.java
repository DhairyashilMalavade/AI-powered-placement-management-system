package com.dhairya.Placement_management_system.drive;

import com.dhairya.Placement_management_system.application.ApplicationRepository;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.drive.dto.CreateDriveRequest;
import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.drive.dto.UpdateDriveRequest;
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
public class DriveService {

    private static final List<String> VALID_STATUSES = List.of("DRAFT", "ACTIVE", "CLOSED", "COMPLETED");

    private final DriveRepository driveRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;

    public DriveService(DriveRepository driveRepository, UserRepository userRepository,
                        ApplicationRepository applicationRepository,
                        FileStorageService fileStorageService) {
        this.driveRepository = driveRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public DriveResponse create(CreateDriveRequest request, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        Drive drive = new Drive();
        drive.setTitle(request.getTitle());
        drive.setDescription(request.getDescription());
        drive.setMinGpa(request.getMinGpa());
        drive.setAllowedGraduationYears(request.getAllowedGraduationYears());
        drive.setRequiredSkills(request.getRequiredSkills());
        drive.setAdditionalCriteria(request.getAdditionalCriteria());
        drive.setApplicationDeadline(request.getApplicationDeadline());
        drive.setDriveDate(request.getDriveDate());
        drive.setCreatedBy(creator);
        drive = driveRepository.save(drive);
        return toResponse(drive);
    }

    public PagedResponse<DriveResponse> getAll(String search, String status, Pageable pageable) {
        Page<DriveResponse> page;
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasSearch && hasStatus) {
            page = driveRepository.searchByTitleAndStatus(search, status, pageable)
                .map(this::toResponse);
        } else if (hasSearch) {
            page = driveRepository.searchByTitle(search, pageable)
                .map(this::toResponse);
        } else if (hasStatus) {
            page = driveRepository.findByStatus(status, pageable)
                .map(this::toResponse);
        } else {
            page = driveRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
        }
        return PagedResponse.from(page);
    }

    public DriveResponse getById(UUID id) {
        Drive drive = findById(id);
        return toResponse(drive);
    }

    @Transactional
    public DriveResponse update(UUID id, UpdateDriveRequest request, UUID currentUserId) {
        Drive drive = findById(id);
        verifyOwner(drive, currentUserId);

        if (request.getTitle() != null) drive.setTitle(request.getTitle());
        if (request.getDescription() != null) drive.setDescription(request.getDescription());
        if (request.getMinGpa() != null) drive.setMinGpa(request.getMinGpa());
        if (request.getAllowedGraduationYears() != null) drive.setAllowedGraduationYears(request.getAllowedGraduationYears());
        if (request.getRequiredSkills() != null) drive.setRequiredSkills(request.getRequiredSkills());
        if (request.getAdditionalCriteria() != null) drive.setAdditionalCriteria(request.getAdditionalCriteria());
        if (request.getApplicationDeadline() != null) drive.setApplicationDeadline(request.getApplicationDeadline());
        if (request.getDriveDate() != null) drive.setDriveDate(request.getDriveDate());

        drive = driveRepository.save(drive);
        return toResponse(drive);
    }

    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        Drive drive = findById(id);
        verifyOwner(drive, currentUserId);

        List<String> resumePaths = applicationRepository.findResumePathsByDriveId(id);
        for (String path : resumePaths) {
            fileStorageService.delete(path);
        }

        driveRepository.delete(drive);
    }

    @Transactional
    public DriveResponse updateStatus(UUID id, String status, UUID currentUserId) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("Invalid status. Accepted: " + VALID_STATUSES);
        }
        Drive drive = findById(id);
        verifyOwner(drive, currentUserId);
        drive.setStatus(status);
        drive = driveRepository.save(drive);
        return toResponse(drive);
    }

    private Drive findById(UUID id) {
        return driveRepository.findByIdWithCreator(id)
            .orElseThrow(() -> new ResourceNotFoundException("Drive", "id", id));
    }

    private void verifyOwner(Drive drive, UUID userId) {
        if (!drive.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("You are not the owner of this drive");
        }
    }

    private DriveResponse toResponse(Drive drive) {
        User creator = drive.getCreatedBy();
        UserResponse creatorResponse = new UserResponse(creator.getId(), creator.getEmail(), creator.getFullName(), creator.getRole(), creator.isActive());
        return new DriveResponse(
            drive.getId(),
            drive.getTitle(),
            drive.getDescription(),
            drive.getMinGpa(),
            drive.getAllowedGraduationYears(),
            drive.getRequiredSkills(),
            drive.getAdditionalCriteria(),
            drive.getApplicationDeadline(),
            drive.getDriveDate(),
            drive.getStatus(),
            creatorResponse,
            drive.getCreatedAt(),
            drive.getUpdatedAt()
        );
    }
}
