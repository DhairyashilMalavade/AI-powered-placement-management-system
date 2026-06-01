package com.dhairya.Placement_management_system.auth;

import com.dhairya.Placement_management_system.auth.dto.AuthResponse;
import com.dhairya.Placement_management_system.auth.dto.LoginRequest;
import com.dhairya.Placement_management_system.auth.dto.RegisterRequest;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.user.*;
import com.dhairya.Placement_management_system.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PlacementOfficerProfileRepository placementOfficerProfileRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       StudentProfileRepository studentProfileRepository,
                       RecruiterProfileRepository recruiterProfileRepository,
                       PlacementOfficerProfileRepository placementOfficerProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.placementOfficerProfileRepository = placementOfficerProfileRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user = userRepository.save(user);
        createProfile(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        UserResponse userResponse = toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BusinessException("Account is deactivated");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        UserResponse userResponse = toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    private void createProfile(User user) {
        switch (user.getRole()) {
            case "STUDENT" -> {
                StudentProfile profile = new StudentProfile();
                profile.setUser(user);
                profile.setCollegeName("Pending");
                profile.setGraduationYear(LocalDate.now().getYear());
                profile.setMajor("Pending");
                studentProfileRepository.save(profile);
            }
            case "RECRUITER" -> {
                RecruiterProfile profile = new RecruiterProfile();
                profile.setUser(user);
                profile.setCompanyName("Pending");
                recruiterProfileRepository.save(profile);
            }
            case "PO" -> {
                PlacementOfficerProfile profile = new PlacementOfficerProfile();
                profile.setUser(user);
                profile.setCollegeName("Pending");
                placementOfficerProfileRepository.save(profile);
            }
        }
    }
}
