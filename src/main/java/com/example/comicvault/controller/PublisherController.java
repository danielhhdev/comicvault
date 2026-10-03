package com.example.comicvault.controller;

import com.example.comicvault.dto.CreatePublisherRequest;
import com.example.comicvault.dto.PublisherResponse;
import com.example.comicvault.dto.UpdatePublisherRequest;
import com.example.comicvault.service.PublisherService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publishers")
class PublisherController {

  private final PublisherService service;

  PublisherController(PublisherService service) {
    this.service = service;
  }

  @GetMapping
  PagedModel<PublisherResponse> list(@PageableDefault(sort = "name") Pageable pageable) {
    return new PagedModel<>(service.list(pageable));
  }

  @GetMapping("/{id}")
  PublisherResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  ResponseEntity<PublisherResponse> create(@Valid @RequestBody CreatePublisherRequest request) {
    PublisherResponse created = service.create(request);
    return ResponseEntity.created(URI.create("/api/v1/publishers/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  PublisherResponse update(
      @PathVariable Long id, @Valid @RequestBody UpdatePublisherRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
