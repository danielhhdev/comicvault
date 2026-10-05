package com.example.comicvault.controller;

import com.example.comicvault.dto.CreateSeriesRequest;
import com.example.comicvault.dto.PatchSeriesRequest;
import com.example.comicvault.dto.SeriesResponse;
import com.example.comicvault.dto.UpdateSeriesRequest;
import com.example.comicvault.service.SeriesService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/series")
class SeriesController {

  private final SeriesService service;

  SeriesController(SeriesService service) {
    this.service = service;
  }

  @GetMapping
  PagedModel<SeriesResponse> list(@PageableDefault(sort = "title") Pageable pageable) {
    return new PagedModel<>(service.list(pageable));
  }

  @GetMapping("/{id}")
  SeriesResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  ResponseEntity<SeriesResponse> create(@Valid @RequestBody CreateSeriesRequest request) {
    SeriesResponse created = service.create(request);
    return ResponseEntity.created(URI.create("/api/v1/series/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  SeriesResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSeriesRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}")
  SeriesResponse patch(@PathVariable Long id, @Valid @RequestBody PatchSeriesRequest request) {
    return service.patch(id, request);
  }
}
