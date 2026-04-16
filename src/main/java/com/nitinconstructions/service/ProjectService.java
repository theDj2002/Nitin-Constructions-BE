package com.nitinconstructions.service;

import com.nitinconstructions.dto.*;
import com.nitinconstructions.entity.Project;
import com.nitinconstructions.entity.ProjectImage;
import com.nitinconstructions.repository.ProjectImageRepository;
import com.nitinconstructions.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository      projectRepo;
    private final ProjectImageRepository imageRepo;
    private final CloudinaryService      cloudinaryService;

    public ProjectService(ProjectRepository projectRepo,
                          ProjectImageRepository imageRepo,
                          CloudinaryService cloudinaryService) {
        this.projectRepo      = projectRepo;
        this.imageRepo        = imageRepo;
        this.cloudinaryService = cloudinaryService;
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
        Project p = findOrThrow(id);
        return ProjectResponse.from(p);
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
        if (req.name()        != null) p.setName(req.name());
        if (req.type()        != null) p.setType(req.type());
        if (req.location()    != null) p.setLocation(req.location());
        if (req.description() != null) p.setDescription(req.description());
        if (req.year()        != null) p.setYear(req.year());
        if (req.isVisible()   != null) p.setIsVisible(req.isVisible());
        if (req.order()       != null) p.setOrder(req.order());
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── TOGGLE VISIBILITY ────────────────────────────────────────────────────
    public ProjectResponse toggleVisibility(Long id) {
        Project p = findOrThrow(id);
        p.setIsVisible(!p.getIsVisible());
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── DELETE PROJECT (also removes images from Cloudinary) ─────────────────
    public void delete(Long id) throws IOException {
        Project p = findOrThrow(id);
        for (ProjectImage img : p.getImages()) {
            try { cloudinaryService.deleteImage(img.getPublicId()); }
            catch (IOException ignored) { /* log but continue */ }
        }
        projectRepo.delete(p);
    }

    // ── ADD IMAGE (from already-uploaded Cloudinary URL) ─────────────────────
    public ProjectResponse addImage(Long projectId, ImageRequest req) {
        Project p = findOrThrow(projectId);
        ProjectImage img = ProjectImage.builder()
            .url(req.url())
            .publicId(req.publicId())
            .caption(req.caption() != null ? req.caption() : "")
            .project(p)
            .build();
        p.getImages().add(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── UPLOAD & ADD IMAGE ────────────────────────────────────────────────────
    public ProjectResponse uploadAndAddImage(Long projectId, MultipartFile file) throws IOException {
        Project p = findOrThrow(projectId);
        UploadResult result = cloudinaryService.uploadImage(file);
        ProjectImage img = ProjectImage.builder()
            .url(result.getUrl())
            .publicId(result.getPublicId())
            .caption("")
            .project(p)
            .build();
        p.getImages().add(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── DELETE IMAGE ──────────────────────────────────────────────────────────
    public ProjectResponse deleteImage(Long projectId, String publicId) throws IOException {
        Project p = findOrThrow(projectId);
        ProjectImage img = p.getImages().stream()
            .filter(i -> i.getPublicId().equals(publicId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Image not found with publicId: " + publicId));
        cloudinaryService.deleteImage(publicId);
        p.getImages().remove(img);
        imageRepo.delete(img);
        return ProjectResponse.from(projectRepo.save(p));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private Project findOrThrow(Long id) {
        return projectRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }
}
