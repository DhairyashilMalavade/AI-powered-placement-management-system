package com.dhairya.Placement_management_system.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_resumes")
@Getter
@Setter
@NoArgsConstructor
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;

    @Column(name = "resume_file_path", length = 500, nullable = false)
    private String resumeFilePath;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    @Column(name = "extracted_experience_years")
    private Integer extractedExperienceYears;

    @Column(name = "extracted_education", columnDefinition = "TEXT")
    private String extractedEducation;

    @Column(name = "parse_version", length = 50, nullable = false)
    private String parseVersion = "tika-v1";

    @Column(name = "parsed_at", nullable = false, updatable = false)
    private LocalDateTime parsedAt;

    @PrePersist
    protected void onCreate() {
        parsedAt = LocalDateTime.now();
    }
}
