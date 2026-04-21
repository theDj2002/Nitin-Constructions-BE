package com.nitinconstructions.controller;

import com.nitinconstructions.dto.ApiResponse;
import com.nitinconstructions.dto.UploadResult;
import com.nitinconstructions.service.ImageKitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final ImageKitService imageKitService;

    public UploadController(ImageKitService imageKitService) {
        this.imageKitService = imageKitService;
    }

    // POST /api/upload/single?projectId=1  — Admin
    @PostMapping("/single")
    public ResponseEntity<ApiResponse<UploadResult>> uploadSingle(
            @RequestParam("image") MultipartFile file,
            @RequestParam("projectId") Long projectId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file provided."));
        }
        UploadResult result = imageKitService.uploadImage(file, projectId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /api/upload/multiple?projectId=1  — Admin (up to 10 files)
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<UploadResult>>> uploadMultiple(
            @RequestParam("images") List<MultipartFile> files,
            @RequestParam("projectId") Long projectId) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No files provided."));
        }
        if (files.size() > 10) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Maximum 10 files allowed."));
        }
        List<UploadResult> results = imageKitService.uploadImages(files, projectId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}