package com.example.comicvault.repository;

import com.example.comicvault.entity.Series;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {

  @Override
  @EntityGraph(attributePaths = "publisher")
  Page<Series> findAll(Pageable pageable);

  @EntityGraph(attributePaths = "publisher")
  Optional<Series> findWithPublisherById(Long id);

  boolean existsByPublisherIdAndTitleKey(Long publisherId, String titleKey);

  boolean existsByPublisherIdAndTitleKeyAndIdNot(Long publisherId, String titleKey, Long id);

  boolean existsByPublisherId(Long publisherId);
}
