package com.nitinconstructions.dto;

import com.nitinconstructions.entity.ProjectImage;
import lombok.Data;

@Data
public class ImageDto {
    private Long   id;
    private String url;
    private String publicId;
    private String caption;

}
