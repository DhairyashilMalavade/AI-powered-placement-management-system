package com.dhairya.Placement_management_system.insights;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InsightsControllerTest extends AbstractIntegrationTest {

    @Test
    void student_ShouldNotAccessSkillGaps() throws Exception {
        String token = registerUser("STUDENT");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/insights/skill-gaps", HttpMethod.GET,
            jsonRequest(token, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void recruiter_ShouldAccessSkillGaps() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");

        String driveBody = """
            {"title":"Insights Drive","applicationDeadline":"%s","status":"ACTIVE","requiredSkills":["java","python"]}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> driveResp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, driveBody), String.class);
        String driveId = objectMapper.readTree(driveResp.getBody()).get("data").get("id").asText();
        rest.exchange("/api/v1/drives/" + driveId + "/status", HttpMethod.PATCH,
            jsonRequest(poToken, "{\"status\":\"ACTIVE\"}"), String.class);

        String jobBody = """
            {"driveId":"%s","title":"Insights Job","description":"Test","vacancies":2}
            """.formatted(driveId);
        rest.exchange("/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, jobBody), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/insights/skill-gaps", HttpMethod.GET,
            jsonRequest(recToken, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void recruiter_ShouldAccessOverview() throws Exception {
        String recToken = registerUser("RECRUITER");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/insights/overview", HttpMethod.GET,
            jsonRequest(recToken, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
