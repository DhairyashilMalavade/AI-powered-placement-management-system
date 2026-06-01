package com.dhairya.Placement_management_system.drive;

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
class DriveControllerTest {

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
        String email = "drive-test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String json = """
            {"email":"%s","password":"password123","fullName":"Test %s","role":"%s"}
            """.formatted(email, role, role);
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/auth/register", HttpMethod.POST, jsonRequest(null, json), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("token").asText();
    }

    @Test
    void po_ShouldCreateDrive() throws Exception {
        String poToken = registerUser("PO");
        String body = """
            {"title":"Test Drive","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, body), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("status").asInt()).isEqualTo(201);
        assertThat(root.get("data").get("title").asText()).isEqualTo("Test Drive");
        assertThat(root.get("data").get("status").asText()).isEqualTo("DRAFT");
    }

    @Test
    void student_ShouldNotCreateDrive() throws Exception {
        String studentToken = registerUser("STUDENT");
        String body = """
            {"title":"Test Drive","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(studentToken, body), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAll_ShouldReturnDrives() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"List Test","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        rest.exchange("/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives", HttpMethod.GET, jsonRequest(poToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").isArray()).isTrue();
        assertThat(root.get("data").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getById_ShouldReturnDrive() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Get Test","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives/" + id, HttpMethod.GET, jsonRequest(poToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("id").asText()).isEqualTo(id);
    }

    @Test
    void update_ShouldModifyDrive() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Before Update","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        String updateBody = "{\"title\":\"After Update\"}";
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives/" + id, HttpMethod.PUT, jsonRequest(poToken, updateBody), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("title").asText()).isEqualTo("After Update");
    }

    @Test
    void updateStatus_ShouldChangeStatus() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Status Test","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        String statusBody = "{\"status\":\"ACTIVE\"}";
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives/" + id + "/status", HttpMethod.PATCH,
            jsonRequest(poToken, statusBody), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    void delete_ShouldRemoveDrive() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Delete Test","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> createResp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);
        String id = objectMapper.readTree(createResp.getBody()).get("data").get("id").asText();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/drives/" + id, HttpMethod.DELETE, jsonRequest(poToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
