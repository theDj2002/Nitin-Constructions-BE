package com.nitinconstructions.controller;

import com.nitinconstructions.dto.*;
import com.nitinconstructions.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean admin) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAll(admin)));
    }

    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getById(id)));
    }

    // POST /api/projects  — Admin
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectRequest req) {
        ProjectResponse created = projectService.create(req);
        return ResponseEntity.status(201).body(ApiResponse.ok("Project created.", created));
    }

    // PUT /api/projects/{id}  — Admin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable Long id,
            @RequestBody ProjectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Project updated.", projectService.update(id, req)));
    }

    // PATCH /api/projects/{id}/toggle  — Admin
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ProjectResponse>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.toggleVisibility(id)));
    }

    // DELETE /api/projects/{id}  — Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) throws IOException {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted.", null));
    }

    // POST /api/projects/{id}/images  — Admin — add image by URL (already uploaded)
    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProjectResponse>> addImage(
            @PathVariable Long id,
            @RequestBody ImageRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.addImage(id, req)));
    }

    // POST /api/projects/{id}/images/upload  — Admin — upload file directly
    @PostMapping("/{id}/images/upload")
    public ResponseEntity<ApiResponse<ProjectResponse>> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(projectService.uploadAndAddImage(id, file)));
    }

    // DELETE /api/projects/{id}/images/{publicId}  — Admin
    @DeleteMapping("/{id}/images/{publicId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> deleteImage(
            @PathVariable Long id,
            @PathVariable String publicId) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("Image removed.", projectService.deleteImage(id, publicId)));
    }
    @PostMapping("/{id}/images/upload-multiple")
    public ResponseEntity<ApiResponse<ProjectResponse>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ok("No files provided.", null));
        }
        if (files.size() > 10) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ok("Maximum 10 images per upload.", null));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                "Uploaded " + files.size() + " image(s).",
                projectService.uploadAndAddImages(id, files)
        ));
    }


}