package com.dhairya.Placement_management_system.ai;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class TikaResumeParserTest {

    private final TikaResumeParser parser = new TikaResumeParser();

    @Test
    void parse_ShouldReturnEmptyResult_OnInvalidContent() {
        InputStream input = new ByteArrayInputStream("not a pdf".getBytes());
        ResumeParseResult result = parser.parse(input, "test.pdf");
        assertThat(result).isNotNull();
        assertThat(result.getSkills()).isNotNull();
        assertThat(result.getEducation()).isNotNull();
    }

    @Test
    void parse_ShouldHandleEmptyStream() {
        InputStream input = new ByteArrayInputStream(new byte[0]);
        ResumeParseResult result = parser.parse(input, "empty.pdf");
        assertThat(result).isNotNull();
    }
}
