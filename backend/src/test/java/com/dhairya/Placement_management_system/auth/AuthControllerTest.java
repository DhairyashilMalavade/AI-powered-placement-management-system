package com.dhairya.Placement_management_system.auth;

import com.dhairya.Placement_management_system.auth.dto.LoginRequest;
import com.dhairya.Placement_management_system.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    private final String password = "password123";
    private final String fullName = "Test User";
    private final String role = "STUDENT";

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private RegisterRequest registerRequest(String email) {
        var request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFullName(fullName);
        request.setRole(role);
        return request;
    }

    @Test
    void register_ShouldReturn201AndToken() throws Exception {
        String email = uniqueEmail();
        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/auth/register", registerRequest(email), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(201);
        assertThat(body.get("data")).isNotNull();
        assertThat(body.get("data").get("token")).isNotNull();
        assertThat(body.get("data").get("user").get("email").asText()).isEqualTo(email);
    }

    @Test
    void registerAndLogin_ShouldReturn200AndToken() throws Exception {
        String email = uniqueEmail();
        rest.postForEntity("/api/v1/auth/register", registerRequest(email), String.class);

        var loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(200);
        assertThat(body.get("data").get("token").asText()).isNotBlank();
        assertThat(body.get("data").get("user").get("email").asText()).isEqualTo(email);
    }

    @Test
    void getMe_WithValidToken_ShouldReturn200() throws Exception {
        String email = uniqueEmail();
        ResponseEntity<String> registerResponse = rest.postForEntity(
            "/api/v1/auth/register", registerRequest(email), String.class);
        String token = objectMapper.readTree(registerResponse.getBody())
            .get("data").get("token").asText();

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(headers);

        ResponseEntity<String> meResponse = rest.exchange(
            "/api/v1/auth/me", HttpMethod.GET, entity, String.class);

        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = objectMapper.readTree(meResponse.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(200);
        assertThat(body.get("data").get("email").asText()).isEqualTo(email);
    }

    @Test
    void getMe_WithoutToken_ShouldReturn401() throws Exception {
        ResponseEntity<String> response = rest.getForEntity(
            "/api/v1/auth/me", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(401);
    }
}
