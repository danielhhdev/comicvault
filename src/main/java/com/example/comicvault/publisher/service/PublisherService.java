package com.example.comicvault.publisher.service;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.publisher.dto.CreatePublisherRequest;
import com.example.comicvault.publisher.dto.PublisherResponse;
import com.example.comicvault.publisher.dto.UpdatePublisherRequest;
import com.example.comicvault.publisher.entity.Publisher;
import com.example.comicvault.publisher.repository.PublisherRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublisherService {

  private static final String RESOURCE = "Editorial";

  private final PublisherRepository repository;

  public PublisherService(PublisherRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Page<PublisherResponse> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public PublisherResponse get(Long id) {
    return toResponse(find(id));
  }

  @Transactional
  public PublisherResponse create(CreatePublisherRequest request) {
    if (repository.existsByNameIgnoreCase(request.name())) {
      throw nameInUse(request.name());
    }
    try {
      return toResponse(repository.saveAndFlush(new Publisher(request.name(), request.country())));
    } catch (DataIntegrityViolationException ex) {
      // Dos peticiones concurrentes pueden pasar existsByName; la restricción única decide.
      throw nameInUse(request.name());
    }
  }

  @Transactional
  public PublisherResponse update(Long id, UpdatePublisherRequest request) {
    Publisher publisher = find(id);
    if (repository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
      throw nameInUse(request.name());
    }
    publisher.setName(request.name());
    publisher.setCountry(request.country());
    try {
      repository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw nameInUse(request.name());
    }
    return toResponse(publisher);
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(find(id));
  }

  private Publisher find(Long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE, id));
  }

  private ConflictException nameInUse(String name) {
    return new ConflictException("Ya existe una editorial con el nombre '%s'".formatted(name));
  }

  private PublisherResponse toResponse(Publisher publisher) {
    return new PublisherResponse(publisher.getId(), publisher.getName(), publisher.getCountry());
  }
}
