package com.dhairya.Placement_management_system.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationControllerTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpEntity<String> jsonRequest(String token, String body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private String registerUser(String role) throws Exception {
        String email = "app-test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String json = """
            {"email":"%s","password":"password123","fullName":"Test %s","role":"%s"}
            """.formatted(email, role, role);
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/auth/register", HttpMethod.POST, jsonRequest(null, json), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("token").asText();
    }

    private String createDrive(String poToken) throws Exception {
        String body = """
            {"title":"App Test Drive","applicationDeadline":"%s","status":"ACTIVE"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, body), String.class);
        String id = objectMapper.readTree(resp.getBody()).get("data").get("id").asText();

        // Activate the drive
        rest.exchange("/api/v1/drives/" + id + "/status", HttpMethod.PATCH,
            jsonRequest(poToken, "{\"status\":\"ACTIVE\"}"), String.class);
        return id;
    }

    private String createJobPost(String recToken, String driveId) throws Exception {
        String body = """
            {"driveId":"%s","title":"SWE Intern","description":"Build cool stuff","vacancies":2}
            """.formatted(driveId);
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/job-posts", HttpMethod.POST, jsonRequest(recToken, body), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("id").asText();
    }

    @Test
    void student_ShouldCreateApplication() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String studentToken = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("status").asInt()).isEqualTo(201);
        assertThat(root.get("data").get("status").asText()).isEqualTo("APPLIED");
    }

    @Test
    void recruiter_ShouldNotCreateApplication() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications", HttpMethod.POST,
            jsonRequest(recToken, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void student_ShouldNotApplyTwice() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String studentToken = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        String body = "{\"jobPostId\":\"" + jobPostId + "\"}";
        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, body), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, body), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recruiter_ShouldViewApplicationsForJobPost() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String studentToken = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications/job-post/" + jobPostId, HttpMethod.GET,
            jsonRequest(recToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").isArray()).isTrue();
        assertThat(root.get("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void recruiter_ShouldUpdateApplicationStatus() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String studentToken = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);
        String appId = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications/" + appId + "/status", HttpMethod.PATCH,
            jsonRequest(recToken, "{\"status\":\"SHORTLISTED\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("status").asText()).isEqualTo("SHORTLISTED");
    }
}
