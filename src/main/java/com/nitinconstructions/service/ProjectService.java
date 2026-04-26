package com.nitinconstructions.service;

import com.nitinconstructions.dto.ImageRequest;
import com.nitinconstructions.dto.ProjectRequest;
import com.nitinconstructions.dto.ProjectResponse;
import com.nitinconstructions.dto.UploadResult;
import com.nitinconstructions.entity.Project;
import com.nitinconstructions.entity.ProjectImage;
import com.nitinconstructions.repository.ProjectImageRepository;
import com.nitinconstructions.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectImageRepository imageRepo;
    private final ImageKitService imageKitService; // ← replaced CloudinaryService

    public ProjectService(ProjectRepository projectRepo,
                          ProjectImageRepository imageRepo,
                          ImageKitService imageKitService) {
        this.projectRepo = projectRepo;
        this.imageRepo = imageRepo;
        this.imageKitService = imageKitService;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<ProjectResponse> getAll(boolean adminView) {
        List<Project> projects = adminView
                ? projectRepo.findAllSorted()
                : projectRepo.findAllVisible();
        return projects.stream().map(ProjectResponse::from).toList();
    }

    // ── GET ONE ──────────────────────────────────────────────────────────────
    public ProjectResponse getById(Long id) {
        return ProjectResponse.from(findOrThrow(id));
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public ProjectResponse create(ProjectRequest req) {
        Project project = Project.builder()
                .name(req.name())
                .type(req.type())
                .location(req.location())
                .description(req.description())
                .year(req.year())
                .isVisible(req.isVisible() != null ? req.isVisible() : true)
                .order(req.order() != null ? req.order() : 0)
                .build();
        return ProjectResponse.from(projectRepo.save(project));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public ProjectResponse update(Long id, ProjectRequest req) {
        Project p = findOrThrow(id);
        if (req.name() != null) p.setName(req.name());
        if (req.type() != null) p.setType(req.type());
        if (req.location() != null) p.setLocation(req.location());
        if (req.description() != null) p.setDescription(req.description());
        if (req.year() != null) p.setYear(req.year());
        if (req.isVisible() != null) p.setIsVisible(req.isVisible());
        if (req.order() != null) p.setOrder(req.order());
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── TOGGLE VISIBILITY ────────────────────────────────────────────────────
    public ProjectResponse toggleVisibility(Long id) {
        Project p = findOrThrow(id);
        p.setIsVisible(!p.getIsVisible());
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── DELETE PROJECT + all ImageKit images ─────────────────────────────────
    public void delete(Long id) {
        Project p = findOrThrow(id);

        // Delete each image individually first (safe fallback)
        for (ProjectImage img : p.getImages()) {
            try {
                imageKitService.deleteImage(img.getPublicId()); // publicId = ImageKit fileId
            } catch (Exception e) {
                // Log and continue — don't fail the whole delete
            }
        }

        // Also delete the folder on ImageKit (cleans up completely)
        try {
            imageKitService.deleteProjectFolder(id);
        } catch (Exception ignored) {
        }

        projectRepo.delete(p);
    }

    // ── ADD IMAGE (from already-uploaded URL — admin flow) ───────────────────
    public ProjectResponse addImage(Long projectId, ImageRequest req) {
        Project p = findOrThrow(projectId);
        ProjectImage img = ProjectImage.builder()
                .url(req.url())
                .publicId(req.publicId())      // store ImageKit fileId here
                .caption(req.caption() != null ? req.caption() : "")
                .project(p)
                .build();
        p.getImages().add(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── UPLOAD & ADD IMAGE (direct file upload) ───────────────────────────────
    public ProjectResponse uploadAndAddImage(Long projectId, MultipartFile file) {
        Project p = findOrThrow(projectId);

        // Upload to ImageKit under /nitin-constructions/projects/{projectId}/
       imageKitService.uploadImage(file, projectId);
//
//        ProjectImage img = ProjectImage.builder()
//                .url(result.getUrl())
//                .publicId(result.getFileId())  // ← ImageKit fileId stored as publicId
//                .caption("")
//                .project(p)
//                .build();
//        p.getImages().add(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── DELETE IMAGE ──────────────────────────────────────────────────────────
    public ProjectResponse deleteImage(Long projectId, String publicId) {
        Project p = findOrThrow(projectId);

        ProjectImage img = p.getImages().stream()
                .filter(i -> i.getPublicId().equals(publicId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Image not found with fileId: " + publicId));

        imageKitService.deleteImage(publicId); // publicId = ImageKit fileId
        p.getImages().remove(img);
        imageRepo.delete(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── HELPER ────────────────────────────────────────────────────────────────
    private Project findOrThrow(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    // ── UPLOAD & ADD MULTIPLE IMAGES ──────────────────────────────────────────
    /**
     * Uploads every file in the list to ImageKit and attaches them all to the
     * project in one transaction.  Partial failures are collected and reported
     * rather than rolling back successfully uploaded images.
     *
     * @throws RuntimeException if ALL uploads fail (so the caller gets a 500).
     */
    public ProjectResponse uploadAndAddImages(Long projectId, List<MultipartFile> files) {
        Project p = findOrThrow(projectId);

        List<String> failures = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
               imageKitService.uploadImage(file, projectId);
//                ProjectImage img = ProjectImage.builder()
//                        .url(result.getUrl())
//                        .publicId(result.getFileId())
//                        .caption("")
//                        .project(p)
//                        .build();
//                p.getImages().add(img);
            } catch (Exception e) {
                failures.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        // Persist whatever succeeded
        ProjectResponse response = ProjectResponse.from(projectRepo.save(p));

        // If every single file failed, surface the error to the controller
        if (failures.size() == files.size()) {
            throw new RuntimeException("All uploads failed: " + String.join("; ", failures));
        }

        return response;
    }
}