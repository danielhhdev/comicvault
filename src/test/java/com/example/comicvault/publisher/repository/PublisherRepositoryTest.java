package com.example.comicvault.publisher.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.comicvault.publisher.entity.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

  @Test
  void laRestriccionUnicaRechazaUnNombreDuplicado() {
    repository.saveAndFlush(new Publisher("Norma", "España"));

    assertThatThrownBy(() -> repository.saveAndFlush(new Publisher("Norma", "Francia")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void ordenarPorUnaPropiedadInexistenteLanzaPropertyReferenceException() {
    assertThatThrownBy(() -> repository.findAll(PageRequest.of(0, 10, Sort.by("foo"))))
        .isInstanceOf(PropertyReferenceException.class);
  }
}
