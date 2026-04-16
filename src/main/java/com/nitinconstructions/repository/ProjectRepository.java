package com.nitinconstructions.repository;

import com.nitinconstructions.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Public: only visible projects, sorted by order then newest first
    @Query("SELECT p FROM Project p WHERE p.isVisible = true ORDER BY p.order ASC, p.createdAt DESC")
    List<Project> findAllVisible();

    // Admin: all projects
    @Query("SELECT p FROM Project p ORDER BY p.order ASC, p.createdAt DESC")
    List<Project> findAllSorted();
}
