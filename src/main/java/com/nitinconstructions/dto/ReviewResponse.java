package com.nitinconstructions.dto;

import com.nitinconstructions.entity.Review;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Long id;
    private Long projectId;      // null for general reviews
    private String projectName;  // null for general reviews
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .projectId(r.getProject() != null ? r.getProject().getId() : null)
                .projectName(r.getProject() != null ? r.getProject().getName() : null)
                .userName(r.getUserName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
