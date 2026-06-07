package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${upload.base-url:http://localhost:8080/api/upload/files}")
    private String baseUrl;

    @PostMapping("/uploadImage")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFilename = UUID.randomUUID().toString() + extension;

        // Save the file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // Build response URL
        String fileUrl = baseUrl + "/" + newFilename;

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);

        return new ApiResponse<>(true, "Image uploaded successfully", response);
    }

    @GetMapping("/files/{filename:.+}")
    public org.springframework.core.io.Resource serveFile(
            @PathVariable String filename,
            HttpServletResponse response
    ) throws IOException {
        Path filePath = Paths.get(uploadDirectory).resolve(filename);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            // Guess content type
            String contentType = java.nio.file.Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            return resource;
        } else {
            throw new RuntimeException("File not found: " + filename);
        }
    }
}
