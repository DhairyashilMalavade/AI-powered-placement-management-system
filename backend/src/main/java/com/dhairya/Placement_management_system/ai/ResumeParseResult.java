package com.dhairya.Placement_management_system.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResumeParseResult {
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private int experienceYears;
    private List<String> education;
    private String extractedText;
}
