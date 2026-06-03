package com.dhairya.Placement_management_system.ai;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TikaResumeParser implements ResumeParser {

    private static final Logger log = LoggerFactory.getLogger(TikaResumeParser.class);
    private final Tika tika = new Tika();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}");
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+)\\s*(\\+\\s*)?years?\\s*(of\\s+)?experience", Pattern.CASE_INSENSITIVE);
    private static final List<String> SKILL_KEYWORDS = List.of(
        "java", "python", "javascript", "typescript", "sql", "spring", "react",
        "angular", "node", "aws", "docker", "kubernetes", "git", "rest", "api",
        "html", "css", "c\\+\\+", "c#", "ruby", "go", "rust", "scala", "kotlin",
        "swift", "php", "vue", "svelte", "express", "django", "flask", "fastapi",
        "mongodb", "postgresql", "mysql", "redis", "kafka", "rabbitmq", "graphql",
        "grpc", "terraform", "ansible", "jenkins", "github actions", "ci/cd",
        "machine learning", "deep learning", "nlp", "tensorflow", "pytorch",
        "agile", "scrum", "jira", "linux", "unix", "bash", "powershell"
    );
    private static final Pattern EDUCATION_PATTERN = Pattern.compile(
        "(bachelor|master|ph\\.?d|doctorate|b\\.?tech|m\\.?tech|b\\.?e|m\\.?e|b\\.?sc|m\\.?sc|b\\.?a|m\\.?a|diploma|associate)" +
        "\\s*(of|in|'s)?\\s*(.+?)(?:\\.|,|$)", Pattern.CASE_INSENSITIVE);

    @Override
    public ResumeParseResult parse(InputStream inputStream, String filename) {
        ResumeParseResult result = new ResumeParseResult();
        result.setSkills(new ArrayList<>());
        result.setEducation(new ArrayList<>());

        try {
            String text = tika.parseToString(inputStream);
            result.setExtractedText(text);
            result.setFullName(extractName(text));
            result.setEmail(extractEmail(text));
            result.setPhone(extractPhone(text));
            result.setSkills(extractSkills(text));
            result.setExperienceYears(extractExperienceYears(text));
            result.setEducation(extractEducation(text));
        } catch (TikaException | IOException e) {
            log.warn("Tika parsing failed for {}", filename, e);
        }

        return result;
    }

    private String extractName(String text) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() < 60) {
                return trimmed;
            }
        }
        return null;
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group().trim() : null;
    }

    private List<String> extractSkills(String text) {
        List<String> found = new ArrayList<>();
        String lower = text.toLowerCase();
        for (String skill : SKILL_KEYWORDS) {
            if (lower.contains(skill)) {
                found.add(skill);
            }
        }
        return found;
    }

    private int extractExperienceYears(String text) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private List<String> extractEducation(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = EDUCATION_PATTERN.matcher(text);
        while (matcher.find()) {
            String degree = matcher.group().trim();
            if (!found.contains(degree)) {
                found.add(degree);
            }
        }
        return found;
    }
}
