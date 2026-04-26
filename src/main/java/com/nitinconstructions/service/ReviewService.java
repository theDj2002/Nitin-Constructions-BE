package com.nitinconstructions.service;

import com.nitinconstructions.dto.ReviewRequest;
import com.nitinconstructions.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {

    /** Submit a new review (for a project or general). */
    ReviewResponse create(ReviewRequest request);

    /** Get all reviews for a specific project. */
    List<ReviewResponse> getByProject(Long projectId);

    /** Get all general (non-project) reviews. */
    List<ReviewResponse> getGeneral();

    /** Get every review — admin only. */
    List<ReviewResponse> getAll();

    /** Delete a review by id — admin only. */
    void delete(Long id);
}
