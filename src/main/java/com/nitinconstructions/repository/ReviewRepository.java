package com.nitinconstructions.repository;

import com.nitinconstructions.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** All reviews for a specific project, newest first. */
    List<Review> findByProject_IdOrderByCreatedAtDesc(Long projectId);

    /** All general (non-project) reviews, newest first. */
    List<Review> findByProjectIsNullOrderByCreatedAtDesc();

    /** All reviews across all projects, newest first (admin view). */
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.project ORDER BY r.createdAt DESC")
    List<Review> findAllWithProject();

    /** Average rating for a specific project. */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.project.id = :projectId")
    Double averageRatingByProjectId(@Param("projectId") Long projectId);

    /** Total review count for a specific project. */
    long countByProject_Id(Long projectId);
}
