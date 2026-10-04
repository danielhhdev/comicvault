package com.example.comicvault.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.comicvault.entity.Publisher;
import com.example.comicvault.entity.Series;
import com.example.comicvault.entity.SeriesStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
class SeriesRepositoryTest {

  @Autowired SeriesRepository repository;
  @Autowired PublisherRepository publisherRepository;
  @Autowired EntityManager entityManager;

  private Publisher publisher(String name) {
    return publisherRepository.saveAndFlush(new Publisher(name, "España"));
  }

  private Series series(Publisher publisher, String title) {
    return repository.saveAndFlush(new Series(publisher, title, SeriesStatus.ONGOING, 10));
  }

  @Test
  void guardaUnaSerieYLeAsignaId() {
    Series saved = series(publisher("Norma"), "Berserk");

    assertThat(saved.getId()).isNotNull();
    assertThat(repository.findById(saved.getId()))
        .get()
        .extracting(Series::getTitle)
        .isEqualTo("Berserk");
  }

  @Test
  void existsByPublisherIdAndTitleKeyDetectaUnTituloYaUsadoEnLaMismaEditorial() {
    Publisher norma = publisher("Norma");
    series(norma, "Berserk");

    assertThat(repository.existsByPublisherIdAndTitleKey(norma.getId(), Series.keyOf("BERSERK")))
        .isTrue();
    assertThat(repository.existsByPublisherIdAndTitleKey(norma.getId(), Series.keyOf("Monster")))
        .isFalse();
  }

  @Test
  void existsByPublisherIdAndTitleKeyNoMezclaEditoriales() {
    Publisher norma = publisher("Norma");
    Publisher panini = publisher("Panini");
    series(norma, "Berserk");

    assertThat(repository.existsByPublisherIdAndTitleKey(panini.getId(), Series.keyOf("Berserk")))
        .isFalse();
  }

  @Test
  void existsByPublisherIdAndTitleKeyAndIdNotIgnoraLaPropiaSerie() {
    Publisher norma = publisher("Norma");
    Series berserk = series(norma, "Berserk");
    Series monster = series(norma, "Monster");
    String key = Series.keyOf("Berserk");

    assertThat(
            repository.existsByPublisherIdAndTitleKeyAndIdNot(norma.getId(), key, berserk.getId()))
        .isFalse();
    assertThat(
            repository.existsByPublisherIdAndTitleKeyAndIdNot(norma.getId(), key, monster.getId()))
        .isTrue();
  }

  @Test
  void laRestriccionUnicaRechazaUnTituloQueSoloCambiaEnMayusculasEnLaMismaEditorial() {
    Publisher norma = publisher("Norma");
    series(norma, "Berserk");

    assertThatThrownBy(() -> series(norma, "bErSeRk"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void permiteElMismoTituloEnEditorialesDistintas() {
    series(publisher("Norma"), "Berserk");

    Series otra = series(publisher("Panini"), "Berserk");

    assertThat(otra.getId()).isNotNull();
  }

  @Test
  void existsByPublisherIdDetectaSiLaEditorialTieneSeries() {
    Publisher norma = publisher("Norma");
    Publisher panini = publisher("Panini");
    series(norma, "Berserk");

    assertThat(repository.existsByPublisherId(norma.getId())).isTrue();
    assertThat(repository.existsByPublisherId(panini.getId())).isFalse();
  }

  @Test
  void laClaveForaneaImpideBorrarUnaEditorialConSeries() {
    Publisher norma = publisher("Norma");
    series(norma, "Berserk");
    entityManager.clear();

    publisherRepository.delete(publisherRepository.findById(norma.getId()).orElseThrow());

    assertThatThrownBy(() -> publisherRepository.flush())
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void laRestriccionCheckRechazaTotalVolumesCero() {
    Publisher norma = publisher("Norma");

    assertThatThrownBy(
            () -> repository.saveAndFlush(new Series(norma, "Berserk", SeriesStatus.ONGOING, 0)))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("CK_SERIES_TOTAL_VOLUMES");
  }

  @Test
  void findAllCargaLaEditorialSinConsultasAdicionales() {
    Publisher norma = publisher("Norma");
    Publisher panini = publisher("Panini");
    series(norma, "Berserk");
    series(norma, "Monster");
    series(panini, "Batman");
    entityManager.clear();

    var page = repository.findAll(PageRequest.of(0, 10));

    assertThat(page.getContent())
        .hasSize(3)
        .allSatisfy(s -> assertThat(Hibernate.isInitialized(s.getPublisher())).isTrue());
  }

  @Test
  void findWithPublisherByIdCargaLaEditorial() {
    Series berserk = series(publisher("Norma"), "Berserk");
    entityManager.clear();

    Series found = repository.findWithPublisherById(berserk.getId()).orElseThrow();

    assertThat(Hibernate.isInitialized(found.getPublisher())).isTrue();
    assertThat(found.getPublisher().getName()).isEqualTo("Norma");
  }

  @Test
  void ordenarPorUnaPropiedadInexistenteLanzaPropertyReferenceException() {
    assertThatThrownBy(() -> repository.findAll(PageRequest.of(0, 10, Sort.by("foo"))))
        .isInstanceOf(PropertyReferenceException.class);
  }

  @Test
  void ordenaPorElNombreDeLaEditorial() {
    series(publisher("Panini"), "Batman");
    series(publisher("Norma"), "Berserk");

    var page = repository.findAll(PageRequest.of(0, 10, Sort.by("publisher.name")));

    assertThat(page.getContent()).extracting(Series::getTitle).containsExactly("Berserk", "Batman");
  }
}
