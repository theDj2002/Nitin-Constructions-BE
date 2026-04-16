package com.nitinconstructions.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ── Project Request ───────────────────────────────────────────────────────────
public record ProjectRequest(
    @NotBlank(message = "Name is required")     String name,
    @NotBlank(message = "Type is required")     String type,
    @NotBlank(message = "Location is required") String location,
    String description,
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be after 2000")
    @Max(value = 2099, message = "Invalid year")
    Integer year,
    Boolean isVisible,
    Integer order
) {}
