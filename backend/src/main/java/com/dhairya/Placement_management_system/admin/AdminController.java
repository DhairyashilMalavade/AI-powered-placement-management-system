package com.dhairya.Placement_management_system.admin;

import com.dhairya.Placement_management_system.admin.dto.SystemStatsResponse;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.common.dto.PagedResponse;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<PagedResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(adminService.listUsers(role, search, pageable));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable UUID id) {
        return ApiResponse.success(adminService.getUser(id));
    }

    @PatchMapping("/users/{id}/role")
    public ApiResponse<UserResponse> updateUserRole(@PathVariable UUID id,
                                                     @RequestBody Map<String, String> body,
                                                     Authentication auth) {
        UUID currentUserId = (UUID) auth.getPrincipal();
        return ApiResponse.success(adminService.updateUserRole(id, body.get("role"), currentUserId));
    }

    @PatchMapping("/users/{id}/active")
    public ApiResponse<UserResponse> toggleUserActive(@PathVariable UUID id, Authentication auth) {
        UUID currentUserId = (UUID) auth.getPrincipal();
        return ApiResponse.success(adminService.toggleUserActive(id, currentUserId));
    }

    @GetMapping("/stats")
    public ApiResponse<SystemStatsResponse> getStats() {
        return ApiResponse.success(adminService.getStats());
    }
}
