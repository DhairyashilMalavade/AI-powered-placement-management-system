package com.dhairya.Placement_management_system.user.dto;

import com.dhairya.Placement_management_system.user.PlacementOfficerProfile;
import com.dhairya.Placement_management_system.user.RecruiterProfile;
import com.dhairya.Placement_management_system.user.StudentProfile;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProfileResponse {

    private UUID id;
    private UUID userId;

    // Student fields
    private String collegeName;
    private Integer graduationYear;
    private String major;
    private BigDecimal gpa;
    private String[] skills;
    private String resumeFilePath;
    private String phone;

    // Recruiter fields
    private String companyName;
    private String companyWebsite;
    private String companyDescription;

    // PO fields
    private String department;

    public static ProfileResponse from(StudentProfile p) {
        ProfileResponse r = new ProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setCollegeName(p.getCollegeName());
        r.setGraduationYear(p.getGraduationYear());
        r.setMajor(p.getMajor());
        r.setGpa(p.getGpa());
        r.setSkills(p.getSkills());
        r.setResumeFilePath(p.getResumeFilePath());
        r.setPhone(p.getPhone());
        return r;
    }

    public static ProfileResponse from(RecruiterProfile p) {
        ProfileResponse r = new ProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setCompanyName(p.getCompanyName());
        r.setCompanyWebsite(p.getCompanyWebsite());
        r.setCompanyDescription(p.getCompanyDescription());
        return r;
    }

    public static ProfileResponse from(PlacementOfficerProfile p) {
        ProfileResponse r = new ProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setCollegeName(p.getCollegeName());
        r.setDepartment(p.getDepartment());
        return r;
    }
}
