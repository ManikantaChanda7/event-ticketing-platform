package com.eventhub.backend.controller;

import com.cloudinary.Cloudinary;
import com.eventhub.backend.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/uploadImage")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return new ApiResponse<>(false, "File is empty", null);
            }

            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                return new ApiResponse<>(false, "File size exceeds 10MB limit", null);
            }

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "resource_type", "auto",
                            "folder", "eventhub"
                    )
            );

            String fileUrl = (String) uploadResult.get("secure_url");

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return new ApiResponse<>(true, "Image uploaded successfully", response);
        } catch (IOException e) {
            return new ApiResponse<>(false, "Failed to read file: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to upload image to Cloudinary: " + e.getMessage(), null);
        }
    }
}
