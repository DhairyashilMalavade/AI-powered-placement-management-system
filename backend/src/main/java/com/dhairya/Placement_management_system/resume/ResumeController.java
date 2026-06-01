package com.dhairya.Placement_management_system.resume;

import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.user.StudentProfile;
import com.dhairya.Placement_management_system.user.StudentProfileRepository;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final FileStorageService fileStorageService;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public ResumeController(FileStorageService fileStorageService,
                            StudentProfileRepository studentProfileRepository,
                            UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public String upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        User student = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        StudentProfile profile = studentProfileRepository.findById(student.getId())
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        String filename = fileStorageService.store(file);
        profile.setResumeFilePath(filename);
        studentProfileRepository.save(profile);

        return filename;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Resource resource = fileStorageService.load(filename);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Resume", "filename", filename);
        }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
