package com.nitinconstructions.controller;

import com.nitinconstructions.dto.ApiResponse;
import com.nitinconstructions.dto.ReviewRequest;
import com.nitinconstructions.dto.ReviewResponse;
import com.nitinconstructions.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * POST /api/reviews
     * Public — anyone can submit a review.
     * Body: { projectId (optional), userName, rating, comment }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse created = reviewService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Review submitted successfully.", created));
    }

    /**
     * GET /api/reviews?projectId={id}
     * Public — returns all reviews for a given project.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByProject(
            @RequestParam Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getByProject(projectId)));
    }

    /**
     * GET /api/reviews/general
     * Public — returns site-level (non-project) reviews.
     */
    @GetMapping("/general")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getGeneral() {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getGeneral()));
    }

    /**
     * GET /api/reviews/all
     * Admin — returns every review across all projects.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAll()));
    }

    /**
     * DELETE /api/reviews/{id}
     * Admin — remove a review.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Review deleted.", null));
    }
}
