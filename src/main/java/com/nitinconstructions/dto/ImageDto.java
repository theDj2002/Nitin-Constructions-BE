package com.nitinconstructions.dto;

import com.nitinconstructions.entity.ProjectImage;

// ── Image DTO ─────────────────────────────────────────────────────────────────
public class ImageDto {
    private Long   id;
    private String url;
    private String publicId;
    private String caption;

    public static ImageDto from(ProjectImage img) {
        ImageDto d = new ImageDto();
        d.id       = img.getId();
        d.url      = img.getUrl();
        d.publicId = img.getPublicId();
        d.caption  = img.getCaption();
        return d;
    }
}
