package com.nitinconstructions.controller;

import com.nitinconstructions.dto.*;
import com.nitinconstructions.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    // POST /api/upload/single  — Admin
    @PostMapping("/single")
    public ResponseEntity<ApiResponse<UploadResult>> uploadSingle(
            @RequestParam("image") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file provided."));
        }
        UploadResult result = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /api/upload/multiple  — Admin (up to 10 files)
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<UploadResult>>> uploadMultiple(
            @RequestParam("images") List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No files provided."));
        }
        if (files.size() > 10) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Maximum 10 files allowed."));
        }
        List<UploadResult> results = cloudinaryService.uploadImages(files);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
