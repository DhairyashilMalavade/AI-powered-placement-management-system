package com.dhairya.Placement_management_system.ai;

import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.user.ParsedResume;
import com.dhairya.Placement_management_system.user.StudentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeywordScorer implements AIScorer {

    private static final Logger log = LoggerFactory.getLogger(KeywordScorer.class);

    @Override
    public ScoringResult score(Drive drive, ParsedResume parsedResume, StudentProfile profile) {
        Set<String> required = parseRequiredSkills(drive.getRequiredSkills());
        Set<String> candidate = parseSkillString(parsedResume.getExtractedSkills());

        int matched = intersection(required, candidate).size();
        int missing = required.size() - matched;

        int skillScore = required.isEmpty() ? 0 : (int) ((double) matched / required.size() * 60);
        boolean gpaOk = profile.getGpa() != null && drive.getMinGpa() != null
                        && profile.getGpa().compareTo(drive.getMinGpa()) >= 0;
        int gpaScore = gpaOk ? 15 : 0;
        int expScore = (parsedResume.getExtractedExperienceYears() != null
                        && parsedResume.getExtractedExperienceYears() >= 1) ? 15 : 0;
        int penalty = missing * 5;

        int finalScore = Math.max(0, Math.min(100, skillScore + gpaScore + expScore - penalty));

        String rationale = String.format(
            "Matched %d/%d required skills. GPA %s. Score: %d/100.",
            matched, required.size(), gpaOk ? "OK" : "below minimum", finalScore);

        String feedback = String.format(
            "Your resume matches %d of %d required skills. %s",
            matched, required.size(), gpaOk ? "" : "Your GPA is below the minimum requirement.");

        return new ScoringResult(finalScore, rationale, feedback, "keyword-v1");
    }

    private Set<String> parseRequiredSkills(String[] skills) {
        if (skills == null) return Set.of();
        return Arrays.stream(skills)
            .map(String::toLowerCase)
            .map(String::trim)
            .collect(Collectors.toSet());
    }

    private Set<String> parseSkillString(String skills) {
        if (skills == null || skills.isBlank()) return Set.of();
        return Arrays.stream(skills.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    private Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }
}
