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

    assertThat(repository.existsByNameIgnoreCase("Norma")).isTrue();
    assertThat(repository.existsByNameIgnoreCase("Planeta")).isFalse();
  }

  @Test
  void existsByNameAndIdNotIgnoraLaPropiaEditorial() {
    Publisher norma = repository.saveAndFlush(new Publisher("Norma", "España"));
    Publisher ecc = repository.saveAndFlush(new Publisher("ECC", "España"));

    assertThat(repository.existsByNameIgnoreCaseAndIdNot("Norma", norma.getId())).isFalse();
    assertThat(repository.existsByNameIgnoreCaseAndIdNot("Norma", ecc.getId())).isTrue();
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

  @Test
  void existsByNameIgnoreCaseIgnoraMayusculasYMinusculas() {
    repository.saveAndFlush(new Publisher("Panini", "Italia"));

    assertThat(repository.existsByNameIgnoreCase("panini")).isTrue();
    assertThat(repository.existsByNameIgnoreCase("PANINI")).isTrue();
  }

  @Test
  void laRestriccionUnicaRechazaUnNombreQueSoloCambiaEnMayusculas() {
    repository.saveAndFlush(new Publisher("Panini", "Italia"));

    assertThatThrownBy(() -> repository.saveAndFlush(new Publisher("pAnInI", "Italia")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void renombrarAUnNombreQueSoloCambiaEnMayusculasViolaLaRestriccionUnica() {
    repository.saveAndFlush(new Publisher("Panini", "Italia"));
    Publisher norma = repository.saveAndFlush(new Publisher("Norma", "España"));

    norma.setName("PANINI");

    assertThatThrownBy(() -> repository.flush())
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
