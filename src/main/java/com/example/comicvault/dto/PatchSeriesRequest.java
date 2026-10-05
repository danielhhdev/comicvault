package com.example.comicvault.dto;

import com.example.comicvault.entity.SeriesStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Campos opcionales: {@code null} significa "sin cambios". */
public record PatchSeriesRequest(
    @Size(max = 200) @Pattern(regexp = "(?s).*\\S.*", message = "no puede estar en blanco")
        String title,
    Long publisherId,
    SeriesStatus status,
    @Positive Integer totalVolumes) {}
