package com.example.comicvault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "series")
public class Series {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "publisher_id", nullable = false)
  private Publisher publisher;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(name = "title_key", nullable = false, length = 200)
  private String titleKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SeriesStatus status;

  @Column(name = "total_volumes")
  private Integer totalVolumes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Series() {}

  public Series(Publisher publisher, String title, SeriesStatus status, Integer totalVolumes) {
    this.publisher = publisher;
    setTitle(title);
    this.status = status;
    this.totalVolumes = totalVolumes;
  }

  /** Clave en minúsculas: la restricción única de la BD ignora mayúsculas y minúsculas. */
  public static String keyOf(String title) {
    return title.toLowerCase(Locale.ROOT);
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
    this.titleKey = keyOf(title);
  }

  public SeriesStatus getStatus() {
    return status;
  }

  public void setStatus(SeriesStatus status) {
    this.status = status;
  }

  public Integer getTotalVolumes() {
    return totalVolumes;
  }

  public void setTotalVolumes(Integer totalVolumes) {
    this.totalVolumes = totalVolumes;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Series series && id != null && Objects.equals(id, series.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
