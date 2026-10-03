package com.example.comicvault.publisher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "publishers")
public class Publisher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "name_key", nullable = false, length = 120)
  private String nameKey;

  @Column(length = 60)
  private String country;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Publisher() {}

  public Publisher(String name, String country) {
    setName(name);
    this.country = country;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    // Clave en minúsculas: la restricción única de la BD ignora mayúsculas y minúsculas.
    this.nameKey = name.toLowerCase(Locale.ROOT);
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
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
