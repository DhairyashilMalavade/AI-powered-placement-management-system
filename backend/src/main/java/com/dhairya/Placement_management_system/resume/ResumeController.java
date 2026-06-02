package com.dhairya.Placement_management_system.resume;

import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.user.StudentProfile;
import com.dhairya.Placement_management_system.user.StudentProfileRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final FileStorageService fileStorageService;
    private final StudentProfileRepository studentProfileRepository;

    public ResumeController(FileStorageService fileStorageService,
                            StudentProfileRepository studentProfileRepository) {
        this.fileStorageService = fileStorageService;
        this.studentProfileRepository = studentProfileRepository;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        requireRole(auth, "ROLE_STUDENT");

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        String oldFile = profile.getResumeFilePath();
        String filename = fileStorageService.store(file);
        profile.setResumeFilePath(filename);
        studentProfileRepository.save(profile);

        if (oldFile != null) {
            fileStorageService.delete(oldFile);
        }

        return filename;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename, Authentication auth) {
        requireRole(auth, "ROLE_STUDENT");

        UUID userId = (UUID) auth.getPrincipal();
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        if (profile.getResumeFilePath() == null || !profile.getResumeFilePath().equals(filename)) {
            throw new AccessDeniedException("You can only download your own resume");
        }

        Resource resource = fileStorageService.load(filename);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Resume", "filename", filename);
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    private static void requireRole(Authentication auth, String expectedRole) {
        boolean hasRole = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(expectedRole::equals);
        if (!hasRole) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
