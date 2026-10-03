package com.example.comicvault.__recurso__;

import com.example.comicvault.__recurso__.dto.Create__Recurso__Request;
import com.example.comicvault.__recurso__.dto.__Recurso__Response;
import com.example.comicvault.common.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class __Recurso__Service {

  private final __Recurso__Repository repository;

  __Recurso__Service(__Recurso__Repository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  Page<__Recurso__Response> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  __Recurso__Response get(Long id) {
    return repository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new NotFoundException("__Recurso__", id));
  }

  @Transactional
  __Recurso__Response create(Create__Recurso__Request request) {
    // TODO: mapear la petición a la entidad, guardar y devolver la respuesta
    throw new UnsupportedOperationException("Pendiente de implementar");
  }

  @Transactional
  void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("__Recurso__", id);
    }
    repository.deleteById(id);
  }

  private __Recurso__Response toResponse(__Recurso__ entity) {
    // TODO: mapear entidad -> DTO de respuesta
    throw new UnsupportedOperationException("Pendiente de implementar");
  }
}
