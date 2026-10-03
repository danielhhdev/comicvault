package com.example.comicvault.publisher;

import org.springframework.data.jpa.repository.JpaRepository;

interface PublisherRepository extends JpaRepository<Publisher, Long> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
