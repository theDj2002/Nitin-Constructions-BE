package com.nitinconstructions.service;

import com.nitinconstructions.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageKitService {

    /**
     * Upload a single image under a project-specific folder.
     * @param file      the image file
     * @param projectId used to build the folder path
     * @return UploadResult containing fileId, url, filePath
     */
    UploadResult uploadImage(MultipartFile file, Long projectId);

    /**
     * Upload multiple images for a project (max 10).
     */
    List<UploadResult> uploadImages(List<MultipartFile> files, Long projectId);

    /**
     * Delete a single image by its ImageKit fileId.
     */
    void deleteImage(String fileId);

    /**
     * Delete the entire folder for a project (called on project deletion).
     */
    void deleteProjectFolder(Long projectId);
}