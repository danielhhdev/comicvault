package com.example.comicvault.service;

import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.dto.Create__Recurso__Request;
import com.example.comicvault.dto.Patch__Recurso__Request;
import com.example.comicvault.dto.Update__Recurso__Request;
import com.example.comicvault.dto.__Recurso__Response;
import com.example.comicvault.entity.__Recurso__;
import com.example.comicvault.repository.__Recurso__Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class __Recurso__Service {

  private final __Recurso__Repository repository;

  public __Recurso__Service(__Recurso__Repository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<__Recurso__Response> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public __Recurso__Response get(Long id) {
    return repository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new NotFoundException("__Recurso__", id));
  }

  @Transactional
  public __Recurso__Response create(Create__Recurso__Request request) {
    // TODO: mapear la petición a la entidad, guardar y devolver la respuesta
    throw new UnsupportedOperationException("Pendiente de implementar");
  }

  @Transactional
  public __Recurso__Response update(Long id, Update__Recurso__Request request) {
    // TODO: buscar (NotFoundException), validar unicidad, copiar todos los campos y devolver
    throw new UnsupportedOperationException("Pendiente de implementar");
  }

  @Transactional
  public __Recurso__Response patch(Long id, Patch__Recurso__Request request) {
    // TODO: buscar (NotFoundException) y copiar solo los campos que no sean null
    throw new UnsupportedOperationException("Pendiente de implementar");
  }

  @Transactional
  public void delete(Long id) {
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
