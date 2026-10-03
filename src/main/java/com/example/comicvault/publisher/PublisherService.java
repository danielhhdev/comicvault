package com.example.comicvault.publisher;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.publisher.dto.CreatePublisherRequest;
import com.example.comicvault.publisher.dto.PublisherResponse;
import com.example.comicvault.publisher.dto.UpdatePublisherRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PublisherService {

  private static final String RESOURCE = "Editorial";

  private final PublisherRepository repository;

  PublisherService(PublisherRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  Page<PublisherResponse> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  PublisherResponse get(Long id) {
    return toResponse(find(id));
  }

  @Transactional
  PublisherResponse create(CreatePublisherRequest request) {
    if (repository.existsByName(request.name())) {
      throw nameInUse(request.name());
    }
    return toResponse(repository.save(new Publisher(request.name(), request.country())));
  }

  @Transactional
  PublisherResponse update(Long id, UpdatePublisherRequest request) {
    Publisher publisher = find(id);
    if (repository.existsByNameAndIdNot(request.name(), id)) {
      throw nameInUse(request.name());
    }
    publisher.setName(request.name());
    publisher.setCountry(request.country());
    return toResponse(publisher);
  }

  @Transactional
  void delete(Long id) {
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
