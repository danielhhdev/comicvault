package com.example.comicvault.publisher.repository;

import com.example.comicvault.publisher.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
