package com.nitinconstructions.dto;

import com.nitinconstructions.entity.Project;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String type;
    private String location;
    private String description;
    private Integer year;
    private Boolean isVisible;
    private Integer order;
    private LocalDateTime createdAt;
    private List<ImageDto> images;
    private LocalDateTime updatedAt;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.type = p.getType();
        r.location = p.getLocation();
        r.description = p.getDescription();
        r.year = p.getYear();
        r.isVisible = p.getIsVisible();
        r.order = p.getOrder();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        r.images = p.getImages().stream().map(t -> {
            ImageDto dto = new ImageDto();
            dto.setPublicId(String.valueOf(t.getPublicId()));
            dto.setUrl(t.getUrl());
            dto.setCaption(t.getCaption());
            dto.setId(t.getId());
            return dto;
        }).toList();
        return r;
    }
}
