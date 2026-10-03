package com.example.comicvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePublisherRequest(
    @NotBlank @Size(max = 120) String name, @Size(max = 60) String country) {}
