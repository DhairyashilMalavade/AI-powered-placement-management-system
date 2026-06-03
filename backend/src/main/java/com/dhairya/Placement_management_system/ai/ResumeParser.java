package com.dhairya.Placement_management_system.ai;

import java.io.InputStream;

public interface ResumeParser {
    ResumeParseResult parse(InputStream inputStream, String filename);
}
