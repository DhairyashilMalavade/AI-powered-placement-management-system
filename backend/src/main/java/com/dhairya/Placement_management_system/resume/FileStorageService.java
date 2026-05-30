package com.dhairya.Placement_management_system.resume;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file);

    Resource load(String filename);

    void delete(String filename);
}
