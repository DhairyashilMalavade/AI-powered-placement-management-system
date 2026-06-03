package com.dhairya.Placement_management_system.analytics;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsControllerTest extends AbstractIntegrationTest {

    @Test
    void admin_ShouldAccessAnalyticsOverview() throws Exception {
        String token = registerUser("ADMIN");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/analytics/overview", HttpMethod.GET,
            jsonRequest(token, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("totalDrives")).isNotNull();
    }

    @Test
    void admin_ShouldAccessDrivePerformance() throws Exception {
        String token = registerUser("ADMIN");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/analytics/drive-performance", HttpMethod.GET,
            jsonRequest(token, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void admin_ShouldAccessApplicationFunnel() throws Exception {
        String token = registerUser("ADMIN");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/analytics/application-funnel", HttpMethod.GET,
            jsonRequest(token, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("statusCounts")).isNotNull();
    }

    @Test
    void student_ShouldNotAccessAnalytics() throws Exception {
        String token = registerUser("STUDENT");
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/analytics/overview", HttpMethod.GET,
            jsonRequest(token, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
