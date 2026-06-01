package com.dhairya.Placement_management_system.jobpost;

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
class JobPostControllerTest {

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
        String email = "jp-test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String json = """
            {"email":"%s","password":"password123","fullName":"Test %s","role":"%s"}
            """.formatted(email, role, role);
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/auth/register", HttpMethod.POST, jsonRequest(null, json), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("token").asText();
    }

    private String createDrive(String poToken) throws Exception {
        String body = """
            {"title":"JobPost Drive","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, body), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("id").asText();
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
        assertThat(root.get("data").isArray()).isTrue();
        assertThat(root.get("data").size()).isGreaterThanOrEqualTo(1);
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
}
