package com.dhairya.Placement_management_system.auth;

import com.dhairya.Placement_management_system.auth.dto.AuthResponse;
import com.dhairya.Placement_management_system.auth.dto.LoginRequest;
import com.dhairya.Placement_management_system.auth.dto.RegisterRequest;
import com.dhairya.Placement_management_system.common.dto.ApiResponse;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.created(response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID principalId)) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Authentication required"));
        }
        UserResponse response = authService.getCurrentUser(principalId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
