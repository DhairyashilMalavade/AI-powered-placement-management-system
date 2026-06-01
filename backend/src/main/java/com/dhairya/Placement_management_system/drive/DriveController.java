package com.dhairya.Placement_management_system.drive;

import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.drive.dto.CreateDriveRequest;
import com.dhairya.Placement_management_system.drive.dto.DriveResponse;
import com.dhairya.Placement_management_system.drive.dto.UpdateDriveRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drives")
public class DriveController {

    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PO')")
    public ApiResponse<DriveResponse> create(@Valid @RequestBody CreateDriveRequest request, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.created(driveService.create(request, userId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DriveResponse>> getAll() {
        return ApiResponse.success(driveService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DriveResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(driveService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PO')")
    public ApiResponse<DriveResponse> update(@PathVariable UUID id,
                                             @Valid @RequestBody UpdateDriveRequest request,
                                             Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(driveService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PO')")
    public void delete(@PathVariable UUID id, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        driveService.delete(id, userId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PO')")
    public ApiResponse<DriveResponse> updateStatus(@PathVariable UUID id,
                                                   @RequestBody Map<String, String> body,
                                                   Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ApiResponse.success(driveService.updateStatus(id, body.get("status"), userId));
    }
}
