package com.nitinconstructions.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nitinconstructions.dto.UploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public UploadResult uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder",             folder,
                "resource_type",      "image",
                "quality",            "auto",
                "fetch_format",       "auto",
                "transformation",     List.of(Map.of("width", 1200, "height", 800, "crop", "limit"))
            )
        );
        String url      = (String) result.get("secure_url");
        String publicId = (String) result.get("public_id");
        return new UploadResult(url, publicId);
    }

    public List<UploadResult> uploadImages(List<MultipartFile> files) throws IOException {
        List<UploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadImage(file));
        }
        return results;
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
