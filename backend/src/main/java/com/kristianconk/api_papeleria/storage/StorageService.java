package com.kristianconk.api_papeleria.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String folder);
    Resource loadAsResource(String filename);
    void delete(String filename);
}
