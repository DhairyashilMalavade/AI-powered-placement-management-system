package com.dhairya.Placement_management_system.user;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileControllerTest extends AbstractIntegrationTest {

    private String loginAsAdmin() throws Exception {
        String json = """
            {"email":"admin@placement.com","password":"Admin@123"}
            """;
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/auth/login", HttpMethod.POST, jsonRequest(null, json), String.class);
        return objectMapper.readTree(resp.getBody()).get("data").get("token").asText();
    }

    @Test
    void admin_ShouldLoadProfile() throws Exception {
        String token = loginAsAdmin();

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/profile/me", HttpMethod.GET, jsonRequest(token, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("status").asInt()).isEqualTo(200);
        assertThat(root.get("data")).isNotNull();
        // Admin has a PlacementOfficerProfile seeded — expect PO fields
        assertThat(root.get("data").get("collegeName").asText()).isEqualTo("Placement College");
    }

    @Test
    void anonymous_ShouldNotAccessProfile() throws Exception {
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/profile/me", HttpMethod.GET, jsonRequest(null, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
