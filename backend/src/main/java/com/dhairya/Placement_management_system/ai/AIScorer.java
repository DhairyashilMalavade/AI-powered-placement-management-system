package com.dhairya.Placement_management_system.ai;

import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.user.ParsedResume;
import com.dhairya.Placement_management_system.user.StudentProfile;

public interface AIScorer {
    ScoringResult score(Drive drive, ParsedResume parsedResume, StudentProfile profile);
}
