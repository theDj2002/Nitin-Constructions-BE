package com.nitinconstructions.service;

import com.nitinconstructions.dto.ReviewRequest;
import com.nitinconstructions.dto.ReviewResponse;
import com.nitinconstructions.entity.Project;
import com.nitinconstructions.entity.Review;
import com.nitinconstructions.repository.ProjectRepository;
import com.nitinconstructions.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProjectRepository projectRepository) {
        this.reviewRepository = reviewRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        Review.ReviewBuilder builder = Review.builder()
                .userName(request.getUserName())
                .rating(request.getRating())
                .comment(request.getComment());

        // Attach to a project if projectId supplied
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Project not found with id: " + request.getProjectId()));
            builder.project(project);
        }

        Review saved = reviewRepository.save(builder.build());
        return ReviewResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProject(Long projectId) {
        // Validate project exists
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
        return reviewRepository.findByProject_IdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getGeneral() {
        return reviewRepository.findByProjectIsNullOrderByCreatedAtDesc()
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAllWithProject()
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}
