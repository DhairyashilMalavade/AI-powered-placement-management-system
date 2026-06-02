package com.dhairya.Placement_management_system.notification;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationControllerTest extends AbstractIntegrationTest {

    @Test
    void getNotifications_ShouldReturnPagedResponse() throws Exception {
        String token = registerUser("STUDENT");

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/notifications", HttpMethod.GET,
            jsonRequest(token, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("totalPages").asInt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void getUnreadCount_ShouldReturnNumber() throws Exception {
        String token = registerUser("STUDENT");

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/notifications/unread-count", HttpMethod.GET,
            jsonRequest(token, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("count").isNumber()).isTrue();
    }

    @Test
    void markAllAsRead_ShouldReturn204() throws Exception {
        String token = registerUser("STUDENT");

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/notifications/read-all", HttpMethod.PATCH,
            jsonRequest(token, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void notifications_WithoutAuth_ShouldReturn401() throws Exception {
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/notifications", HttpMethod.GET,
            jsonRequest(null, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
