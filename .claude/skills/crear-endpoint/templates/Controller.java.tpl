package com.example.comicvault.__recurso__;

import com.example.comicvault.__recurso__.dto.Create__Recurso__Request;
import com.example.comicvault.__recurso__.dto.__Recurso__Response;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  Page<__Recurso__Response> list(Pageable pageable) {
    return service.list(pageable);
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

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
