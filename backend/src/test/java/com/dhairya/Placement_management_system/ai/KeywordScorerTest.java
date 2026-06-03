package com.dhairya.Placement_management_system.ai;

import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.user.ParsedResume;
import com.dhairya.Placement_management_system.user.StudentProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordScorerTest {

    private final KeywordScorer scorer = new KeywordScorer();
    private Drive drive;
    private ParsedResume parsedResume;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        drive = new Drive();
        drive.setRequiredSkills(new String[]{"java", "spring", "sql"});
        drive.setMinGpa(new BigDecimal("3.0"));

        parsedResume = new ParsedResume();
        parsedResume.setExtractedSkills("Java, Python, SQL");
        parsedResume.setExtractedExperienceYears(2);

        profile = new StudentProfile();
        profile.setGpa(new BigDecimal("3.5"));
    }

    @Test
    void score_ShouldReturnScore_WhenAllCriteriaMatch() {
        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getScore()).isBetween(0, 100);
        assertThat(result.getRationale()).contains("Matched");
        assertThat(result.getFeedback()).contains("matches");
        assertThat(result.getVersion()).isEqualTo("keyword-v1");
    }

    @Test
    void score_ShouldReturnLowScore_WhenNoSkillsMatch() {
        parsedResume.setExtractedSkills("none, irrelevant");

        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getScore()).isLessThanOrEqualTo(30);
    }

    @Test
    void score_ShouldApplyPenalty_WhenGpaBelowMinimum() {
        profile.setGpa(new BigDecimal("2.0"));

        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getRationale()).contains("below minimum");
        assertThat(result.getScore()).isLessThan(70);
    }

    @Test
    void score_ShouldReturnZeroExperienceScore_WhenNoExperience() {
        parsedResume.setExtractedExperienceYears(0);

        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getScore()).isLessThan(85);
    }

    @Test
    void score_ShouldHandleNullSkills() {
        drive.setRequiredSkills(null);

        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getScore()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void score_ShouldHandleNullExtractedSkills() {
        parsedResume.setExtractedSkills(null);

        ScoringResult result = scorer.score(drive, parsedResume, profile);

        assertThat(result.getScore()).isLessThanOrEqualTo(30);
    }

    @Test
    void score_ShouldBeBetweenZeroAndOneHundred() {
        for (int i = 0; i < 20; i++) {
            parsedResume.setExtractedSkills("skill" + i);
            ScoringResult result = scorer.score(drive, parsedResume, profile);
            assertThat(result.getScore()).isBetween(0, 100);
        }
    }
}
