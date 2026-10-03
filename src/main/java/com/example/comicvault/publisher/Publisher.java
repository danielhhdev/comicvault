package com.example.comicvault.publisher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "publishers")
class Publisher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(length = 60)
  private String country;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Publisher() {}

  Publisher(String name, String country) {
    this.name = name;
    this.country = country;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }

  Long getId() {
    return id;
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  String getCountry() {
    return country;
  }

  void setCountry(String country) {
    this.country = country;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Publisher publisher && id != null && Objects.equals(id, publisher.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
