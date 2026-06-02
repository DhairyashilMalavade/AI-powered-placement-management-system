package com.dhairya.Placement_management_system.jobpost;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobPostControllerTest extends AbstractIntegrationTest {

    private String createDrive(String poToken) throws Exception {
        String body = """
            {"title":"JobPost Drive","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, body), String.class);
        String driveId = objectMapper.readTree(resp.getBody()).get("data").get("id").asText();
        rest.exchange("/api/v1/drives/" + driveId + "/status", HttpMethod.PATCH,
            jsonRequest(poToken, "{\"status\":\"ACTIVE\"}"), String.class);
        return driveId;
    }

    @Test
    void recruiter_ShouldCreateJobPost() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);

        String body = """
            {"driveId":"%s","title":"SWE Intern","description":"Build cool stuff","vacancies":2}
            """.formatted(driveId);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, body), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("status").asInt()).isEqualTo(201);
        assertThat(root.get("data").get("title").asText()).isEqualTo("SWE Intern");
        assertThat(root.get("data").get("status").asText()).isEqualTo("OPEN");
    }

    @Test
    void student_ShouldNotCreateJobPost() throws Exception {
        String poToken = registerUser("PO");
        String studentToken = registerUser("STUDENT");
        String driveId = createDrive(poToken);

        String body = """
            {"driveId":"%s","title":"Test","description":"Test","vacancies":1}
            """.formatted(driveId);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts", HttpMethod.POST, jsonRequest(studentToken, body), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getByDrive_ShouldReturnJobPosts() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);

        String createBody = """
            {"driveId":"%s","title":"Role A","description":"Desc A","vacancies":1}
            """.formatted(driveId);
        rest.exchange("/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, createBody), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts/drive/" + driveId, HttpMethod.GET, jsonRequest(recToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("data").get("totalPages").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void updateStatus_ShouldChangeStatus() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);

        String createBody = """
            {"driveId":"%s","title":"Status Test","description":"Test","vacancies":1}
            """.formatted(driveId);
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts/" + id + "/status", HttpMethod.PATCH,
            jsonRequest(recToken, "{\"status\":\"FILLED\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("status").asText()).isEqualTo("FILLED");
    }

    @Test
    void delete_ShouldRemoveJobPost() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);

        String createBody = """
            {"driveId":"%s","title":"Delete Test","description":"Test","vacancies":1}
            """.formatted(driveId);
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts/" + id, HttpMethod.DELETE, jsonRequest(recToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getByDrive_ShouldFilterBySearchAndStatus() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);

        String createBody = """
            {"driveId":"%s","title":"Unique Job Role","description":"Test","vacancies":1}
            """.formatted(driveId);
        rest.exchange("/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, createBody), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/job-posts/drive/" + driveId + "?search=Unique&status=OPEN",
            HttpMethod.GET, jsonRequest(recToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("data").get("content").get(0).get("title").asText()).contains("Unique");
    }
}
