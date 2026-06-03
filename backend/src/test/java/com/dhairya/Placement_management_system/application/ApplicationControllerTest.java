package com.dhairya.Placement_management_system.application;

import com.dhairya.Placement_management_system.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationControllerTest extends AbstractIntegrationTest {

    private String createDrive(String poToken) throws Exception {
        String body = """
            {"title":"App Test Drive","applicationDeadline":"%s","status":"ACTIVE"}
            """.formatted(LocalDateTime.now().plusDays(30));
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/drives", HttpMethod.POST, jsonRequest(poToken, body), String.class);
        String id = objectMapper.readTree(resp.getBody()).get("data").get("id").asText();

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

    private HttpEntity<MultiValueMap<String, Object>> multipartRequest(String token, byte[] content) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (token != null) headers.setBearerAuth(token);
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() { return "resume.pdf"; }
        });
        return new HttpEntity<>(body, headers);
    }

    private String uploadResume(String studentToken) throws Exception {
        ResponseEntity<String> resp = rest.exchange(
            "/api/v1/resumes/upload", HttpMethod.POST,
            multipartRequest(studentToken, "%PDF-1.4 test".getBytes()), String.class);
        return resp.getBody().replaceAll("^\"|\"$", "");
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
        assertThat(root.get("data").get("content").isArray()).isTrue();
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void application_ShouldIncludeResumeSnapshotPath_WhenResumeUploaded() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String studentToken = registerUser("STUDENT");

        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        String filename = uploadResume(studentToken);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications", HttpMethod.POST,
            jsonRequest(studentToken, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode root = objectMapper.readTree(response.getBody());
        String snapshot = root.get("data").get("resumeSnapshotPath").asText();
        assertThat(snapshot).isEqualTo(filename);
    }

    @Test
    void student_ShouldSeeOnlyOwnEntryInRankedList() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String student1Token = registerUser("STUDENT");
        String student2Token = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(student1Token, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);
        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(student2Token, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications/job-post/" + jobPostId + "/ranked", HttpMethod.GET,
            jsonRequest(student1Token, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("content").size()).isEqualTo(1);
    }

    @Test
    void recruiter_ShouldSeeAllEntriesInRankedList() throws Exception {
        String poToken = registerUser("PO");
        String recToken = registerUser("RECRUITER");
        String student1Token = registerUser("STUDENT");
        String student2Token = registerUser("STUDENT");
        String driveId = createDrive(poToken);
        String jobPostId = createJobPost(recToken, driveId);

        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(student1Token, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);
        rest.exchange("/api/v1/applications", HttpMethod.POST,
            jsonRequest(student2Token, "{\"jobPostId\":\"" + jobPostId + "\"}"), String.class);

        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications/job-post/" + jobPostId + "/ranked", HttpMethod.GET,
            jsonRequest(recToken, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.get("data").get("content").size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void unauthenticatedUser_ShouldNotAccessRankedList() throws Exception {
        ResponseEntity<String> response = rest.exchange(
            "/api/v1/applications/job-post/nonexistent/ranked", HttpMethod.GET,
            jsonRequest(null, null), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
