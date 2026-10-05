package com.example.comicvault.service;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.dto.CreatePublisherRequest;
import com.example.comicvault.dto.PublisherResponse;
import com.example.comicvault.dto.UpdatePublisherRequest;
import com.example.comicvault.entity.Publisher;
import com.example.comicvault.repository.PublisherRepository;
import com.example.comicvault.repository.SeriesRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublisherService {

  private static final String RESOURCE = "Editorial";

  private final PublisherRepository repository;
  private final SeriesRepository seriesRepository;

  public PublisherService(PublisherRepository repository, SeriesRepository seriesRepository) {
    this.repository = repository;
    this.seriesRepository = seriesRepository;
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
    Publisher publisher = find(id);
    if (seriesRepository.existsByPublisherId(id)) {
      throw hasSeries(id);
    }
    try {
      repository.delete(publisher);
      repository.flush();
    } catch (DataIntegrityViolationException ex) {
      // Una serie puede crearse entre la comprobación y el borrado; la clave foránea decide.
      throw hasSeries(id);
    }
  }

  private Publisher find(Long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE, id));
  }

  private ConflictException nameInUse(String name) {
    return new ConflictException("Ya existe una editorial con el nombre '%s'".formatted(name));
  }

  private ConflictException hasSeries(Long id) {
    return new ConflictException(
        "No se puede borrar la editorial %d porque tiene series".formatted(id));
  }

  private PublisherResponse toResponse(Publisher publisher) {
    return new PublisherResponse(publisher.getId(), publisher.getName(), publisher.getCountry());
  }
}
