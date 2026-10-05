package com.example.comicvault.service;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.dto.CreateSeriesRequest;
import com.example.comicvault.dto.PatchSeriesRequest;
import com.example.comicvault.dto.SeriesResponse;
import com.example.comicvault.dto.UpdateSeriesRequest;
import com.example.comicvault.entity.Publisher;
import com.example.comicvault.entity.Series;
import com.example.comicvault.repository.PublisherRepository;
import com.example.comicvault.repository.SeriesRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeriesService {

  private static final String RESOURCE = "Serie";
  private static final String PUBLISHER_RESOURCE = "Editorial";

  private final SeriesRepository repository;
  private final PublisherRepository publisherRepository;

  public SeriesService(SeriesRepository repository, PublisherRepository publisherRepository) {
    this.repository = repository;
    this.publisherRepository = publisherRepository;
  }

  @Transactional(readOnly = true)
  public Page<SeriesResponse> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public SeriesResponse get(Long id) {
    return toResponse(find(id));
  }

  @Transactional
  public SeriesResponse create(CreateSeriesRequest request) {
    Publisher publisher = findPublisher(request.publisherId());
    if (repository.existsByPublisherIdAndTitleKey(
        publisher.getId(), Series.keyOf(request.title()))) {
      throw titleInUse(request.title());
    }
    try {
      return toResponse(
          repository.saveAndFlush(
              new Series(publisher, request.title(), request.status(), request.totalVolumes())));
    } catch (DataIntegrityViolationException ex) {
      // Dos peticiones concurrentes pueden pasar la comprobación; la restricción única decide.
      throw titleInUse(request.title());
    }
  }

  @Transactional
  public SeriesResponse update(Long id, UpdateSeriesRequest request) {
    Series series = find(id);
    Publisher publisher = findPublisher(request.publisherId());
    checkTitleFree(publisher.getId(), request.title(), id);
    series.setPublisher(publisher);
    series.setTitle(request.title());
    series.setStatus(request.status());
    series.setTotalVolumes(request.totalVolumes());
    flush(request.title());
    return toResponse(series);
  }

  @Transactional
  public SeriesResponse patch(Long id, PatchSeriesRequest request) {
    Series series = find(id);
    Publisher publisher =
        request.publisherId() != null
            ? findPublisher(request.publisherId())
            : series.getPublisher();
    String title = request.title() != null ? request.title() : series.getTitle();
    if (request.publisherId() != null || request.title() != null) {
      checkTitleFree(publisher.getId(), title, id);
    }
    series.setPublisher(publisher);
    series.setTitle(title);
    if (request.status() != null) {
      series.setStatus(request.status());
    }
    if (request.totalVolumes() != null) {
      series.setTotalVolumes(request.totalVolumes());
    }
    flush(title);
    return toResponse(series);
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(find(id));
  }

  private void checkTitleFree(Long publisherId, String title, Long id) {
    if (repository.existsByPublisherIdAndTitleKeyAndIdNot(publisherId, Series.keyOf(title), id)) {
      throw titleInUse(title);
    }
  }

  private void flush(String title) {
    try {
      repository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw titleInUse(title);
    }
  }

  private Series find(Long id) {
    return repository
        .findWithPublisherById(id)
        .orElseThrow(() -> new NotFoundException(RESOURCE, id));
  }

  private Publisher findPublisher(Long publisherId) {
    return publisherRepository
        .findById(publisherId)
        .orElseThrow(() -> new NotFoundException(PUBLISHER_RESOURCE, publisherId));
  }

  private ConflictException titleInUse(String title) {
    return new ConflictException(
        "Ya existe una serie con el título '%s' en esa editorial".formatted(title));
  }

  private SeriesResponse toResponse(Series series) {
    Publisher publisher = series.getPublisher();
    return new SeriesResponse(
        series.getId(),
        series.getTitle(),
        publisher.getId(),
        publisher.getName(),
        series.getStatus(),
        series.getTotalVolumes());
  }
}
