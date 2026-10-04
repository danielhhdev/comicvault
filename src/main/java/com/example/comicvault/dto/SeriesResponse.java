package com.example.comicvault.dto;

import com.example.comicvault.entity.SeriesStatus;

public record SeriesResponse(
    Long id,
    String title,
    Long publisherId,
    String publisherName,
    SeriesStatus status,
    Integer totalVolumes) {}
