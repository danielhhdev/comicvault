package com.example.comicvault.controller;

import com.example.comicvault.dto.Create__Recurso__Request;
import com.example.comicvault.dto.Patch__Recurso__Request;
import com.example.comicvault.dto.Update__Recurso__Request;
import com.example.comicvault.dto.__Recurso__Response;
import com.example.comicvault.service.__Recurso__Service;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/__recursos__")
class __Recurso__Controller {

  private final __Recurso__Service service;

  __Recurso__Controller(__Recurso__Service service) {
    this.service = service;
  }

  @GetMapping
  PagedModel<__Recurso__Response> list(@PageableDefault(sort = "id") Pageable pageable) {
    return new PagedModel<>(service.list(pageable));
  }

  @GetMapping("/{id}")
  __Recurso__Response get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  ResponseEntity<__Recurso__Response> create(@Valid @RequestBody Create__Recurso__Request request) {
    __Recurso__Response created = service.create(request);
    return ResponseEntity.created(URI.create("/api/v1/__recursos__/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  __Recurso__Response update(
      @PathVariable Long id, @Valid @RequestBody Update__Recurso__Request request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}")
  __Recurso__Response patch(
      @PathVariable Long id, @Valid @RequestBody Patch__Recurso__Request request) {
    return service.patch(id, request);
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
