package com.nitinconstructions.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageRequest(
    @NotBlank String url,
    @NotBlank String publicId,
    String caption
) {}
