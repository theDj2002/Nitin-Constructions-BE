package com.nitinconstructions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {

    // ImageKit fileId — used for deletion (replaces Cloudinary's publicId)
    private String fileId;

    // Full CDN URL of the uploaded image
    private String url;

    // Optional: ImageKit file path (e.g. /nitin-constructions/projects/1/image-1.jpg)
    private String filePath;
}