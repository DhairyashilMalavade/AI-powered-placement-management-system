package com.dhairya.Placement_management_system.drive;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DriveControllerTest extends AbstractIntegrationTest {

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
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("data").get("totalPages").asInt()).isGreaterThanOrEqualTo(1);
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

    @Test
    void getAll_ShouldFilterBySearchAndStatus() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Searchable Drive","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        rest.exchange("/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);

        ResponseEntity<String> searchResp = rest.exchange(
            "/api/v1/drives?search=Searchable&status=DRAFT",
            HttpMethod.GET, jsonRequest(poToken, null), String.class);

        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(searchResp.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("data").get("content").get(0).get("title").asText()).contains("Searchable");
    }

    @Test
    void getAll_ShouldFilterBySearchOnly() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"SearchMe Please","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        rest.exchange("/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);

        ResponseEntity<String> searchResp = rest.exchange(
            "/api/v1/drives?search=SearchMe",
            HttpMethod.GET, jsonRequest(poToken, null), String.class);

        assertThat(searchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(searchResp.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(root.get("data").get("content").get(0).get("title").asText()).contains("SearchMe");
    }

    @Test
    void getAll_ShouldFilterByStatusOnly() throws Exception {
        String poToken = registerUser("PO");
        String createBody = """
            {"title":"Status Filter Test","applicationDeadline":"%s"}
            """.formatted(LocalDateTime.now().plusDays(30));
        rest.exchange("/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, createBody), String.class);

        ResponseEntity<String> statusResp = rest.exchange(
            "/api/v1/drives?status=DRAFT",
            HttpMethod.GET, jsonRequest(poToken, null), String.class);

        assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(statusResp.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
    }
}
