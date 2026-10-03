package com.example.comicvault.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PublisherRepositoryTest {

  @Autowired PublisherRepository repository;

  @Test
  void guardaUnaEditorialYLeAsignaId() {
    Publisher saved = repository.saveAndFlush(new Publisher("Panini", "Italia"));

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId()))
        .get()
        .extracting(Publisher::getName)
        .isEqualTo("Panini");
  }

  @Test
  void existsByNameDetectaUnNombreYaUsado() {
    repository.saveAndFlush(new Publisher("Norma", "España"));

    assertThat(repository.existsByName("Norma")).isTrue();
    assertThat(repository.existsByName("Planeta")).isFalse();
  }

  @Test
  void existsByNameAndIdNotIgnoraLaPropiaEditorial() {
    Publisher norma = repository.saveAndFlush(new Publisher("Norma", "España"));
    Publisher ecc = repository.saveAndFlush(new Publisher("ECC", "España"));

    assertThat(repository.existsByNameAndIdNot("Norma", norma.getId())).isFalse();
    assertThat(repository.existsByNameAndIdNot("Norma", ecc.getId())).isTrue();
  }
}
