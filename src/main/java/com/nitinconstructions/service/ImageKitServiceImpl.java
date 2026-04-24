package com.nitinconstructions.service;

import com.nitinconstructions.dto.UploadResult;
import com.nitinconstructions.entity.Project;
import com.nitinconstructions.entity.ProjectImage;
import com.nitinconstructions.exception.ImageUploadException;
import com.nitinconstructions.repository.ProjectRepository;
import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.exceptions.*;
import io.imagekit.sdk.models.DeleteFolderRequest;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageKitServiceImpl implements ImageKitService {

    private final ImageKit imageKit;
    private final ProjectRepository projectRepo;

    @Value("${imagekit.base-folder}")
    private String baseFolder;

    @Override
    public UploadResult uploadImage(MultipartFile file, Long projectId) {
        validateFile(file);
        try {
            String base64 = encodeToBase64(file);
            String fileName = buildFileName(file.getOriginalFilename());
            String folder = buildFolderPath(projectId);

            FileCreateRequest request = new FileCreateRequest(base64, fileName);
            request.setFolder(folder);
            request.setUseUniqueFileName(false);

            // SDK v2+: throws exception on failure — no isSuccessful() check needed
            Result result = imageKit.upload(request);
            Project p = findOrThrow(projectId);
            ProjectImage img = new ProjectImage();
            img = ProjectImage.builder()
                    .url(result.getUrl())
                    .publicId(result.getFileId())
                    .caption("")
                    .project(p)
                    .build();
            p.getImages().add(img);
            projectRepo.save(p);

            log.info(String.valueOf(result));
            log.info("ImageKit upload success | project={} | fileId={} | url={}",
                    projectId, result.getFileId(), result.getUrl());

            return UploadResult.builder()
                    .fileId(result.getFileId())
                    .url(result.getUrl())
                    .filePath(result.getFilePath())
                    .build();

        } catch (IOException e) {
            log.error("Failed to read file bytes for upload | project={}", projectId, e);
            throw new ImageUploadException("Failed to read image file: " + e.getMessage());
        } catch (InternalServerException | ForbiddenException | TooManyRequestsException |
                 UnauthorizedException | BadRequestException | UnknownException e) {
            log.error("ImageKit SDK error | project={} | error={}", projectId, e.getMessage());
            throw new ImageUploadException("ImageKit upload failed: " + e.getMessage());
        }
    }

    private Project findOrThrow(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    @Override
    public List<UploadResult> uploadImages(List<MultipartFile> files, Long projectId) {
        List<UploadResult> results = new ArrayList<>();
//        Project p = findOrThrow(projectId);
//        UploadResult result = new UploadResult();
//        ProjectImage img = new ProjectImage();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                log.warn("Skipping empty file at index {} for project={}", i, projectId);
                continue;
            }
//            result = uploadImage(file, projectId);
//            img = ProjectImage.builder()
//                    .url(result.getUrl())
//                    .publicId(result.getFileId())
//                    .caption("")
//                    .project(p)
//                    .build();
//            p.getImages().add(img);
//            projectRepo.save(p);
            results.add(uploadImage(file, projectId));
        }
        log.info("Uploaded {}/{} images for project={}", results.size(), files.size(), projectId);
        return results;
    }

    // ─────────────────────────────────────────────────────────────
    // Delete single image by fileId
    // ─────────────────────────────────────────────────────────────
    @Override
    public void deleteImage(String fileId) {
        try {
            imageKit.deleteFile(fileId);
            log.info("Deleted ImageKit file | fileId={}", fileId);
        } catch (ForbiddenException | TooManyRequestsException | InternalServerException |
                 UnauthorizedException | BadRequestException | UnknownException e) {
            log.error("Failed to delete ImageKit file | fileId={} | error={}", fileId, e.getMessage());
            throw new ImageUploadException("Failed to delete image: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Delete entire project folder
    // ─────────────────────────────────────────────────────────────
    @Override
    public void deleteProjectFolder(Long projectId) {
        try {
            // SDK v2+: deleteFolder requires a DeleteFolderRequest object
            DeleteFolderRequest deleteFolderRequest = new DeleteFolderRequest();
            deleteFolderRequest.setFolderPath(buildFolderPath(projectId));
            imageKit.deleteFolder(deleteFolderRequest);
            log.info("Deleted ImageKit folder for project={}", projectId);
        } catch (ForbiddenException | TooManyRequestsException | InternalServerException |
                 UnauthorizedException | BadRequestException | UnknownException e) {
            log.error("Failed to delete ImageKit folder | project={} | error={}", projectId, e.getMessage());
            throw new ImageUploadException("Failed to delete project folder: " + e.getMessage());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Folder: nitin-constructions/projects/{projectId}
     */
    private String buildFolderPath(Long projectId) {
        return baseFolder + "/projects/" + projectId;
    }

    private String buildFileName(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "img-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + ext;
    }

    private String encodeToBase64(MultipartFile file) throws IOException {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File must not be null or empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("Only image files are allowed. Received: " + contentType);
        }
    }
}