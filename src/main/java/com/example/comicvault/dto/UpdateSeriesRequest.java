package com.example.comicvault.dto;

import com.example.comicvault.entity.SeriesStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSeriesRequest(
    @NotBlank @Size(max = 200) String title,
    @NotNull Long publisherId,
    @NotNull SeriesStatus status,
    @Positive Integer totalVolumes) {}
