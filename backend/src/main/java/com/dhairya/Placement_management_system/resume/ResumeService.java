package com.dhairya.Placement_management_system.resume;

import com.dhairya.Placement_management_system.ai.ResumeParseResult;
import com.dhairya.Placement_management_system.ai.ResumeParser;
import com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException;
import com.dhairya.Placement_management_system.user.ParsedResume;
import com.dhairya.Placement_management_system.user.ParsedResumeRepository;
import com.dhairya.Placement_management_system.user.StudentProfile;
import com.dhairya.Placement_management_system.user.StudentProfileRepository;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final FileStorageService fileStorageService;
    private final StudentProfileRepository studentProfileRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final UserRepository userRepository;
    private final ResumeParser resumeParser;

    public ResumeService(FileStorageService fileStorageService,
                         StudentProfileRepository studentProfileRepository,
                         ParsedResumeRepository parsedResumeRepository,
                         UserRepository userRepository,
                         ResumeParser resumeParser) {
        this.fileStorageService = fileStorageService;
        this.studentProfileRepository = studentProfileRepository;
        this.parsedResumeRepository = parsedResumeRepository;
        this.userRepository = userRepository;
        this.resumeParser = resumeParser;
    }

    @Transactional
    public String upload(MultipartFile file, UUID userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "userId", userId));

        String oldFile = profile.getResumeFilePath();
        String filename = fileStorageService.store(file);
        profile.setResumeFilePath(filename);
        studentProfileRepository.save(profile);

        if (oldFile != null) {
            fileStorageService.delete(oldFile);
        }

        parseAndStore(file, filename, userId);

        return filename;
    }

    private void parseAndStore(MultipartFile file, String filename, UUID userId) {
        try {
            ResumeParseResult parseResult = resumeParser.parse(file.getInputStream(), filename);

            User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            ParsedResume parsed = parsedResumeRepository.findByStudentId(userId)
                .orElse(new ParsedResume());

            parsed.setStudent(student);
            parsed.setResumeFilePath(filename);
            parsed.setExtractedText(parseResult.getExtractedText());
            parsed.setExtractedSkills(parseResult.getSkills() != null
                ? String.join(", ", parseResult.getSkills()) : null);
            parsed.setExtractedExperienceYears(parseResult.getExperienceYears() >= 0
                ? parseResult.getExperienceYears() : null);
            parsed.setExtractedEducation(parseResult.getEducation() != null
                ? String.join(", ", parseResult.getEducation()) : null);

            parsedResumeRepository.save(parsed);
        } catch (IOException e) {
            log.warn("Failed to parse resume {} for user {}", filename, userId, e);
        }
    }
}
