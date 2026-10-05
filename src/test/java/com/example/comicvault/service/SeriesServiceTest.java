package com.example.comicvault.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.dto.CreateSeriesRequest;
import com.example.comicvault.dto.PatchSeriesRequest;
import com.example.comicvault.dto.SeriesResponse;
import com.example.comicvault.dto.UpdateSeriesRequest;
import com.example.comicvault.entity.Publisher;
import com.example.comicvault.entity.Series;
import com.example.comicvault.entity.SeriesStatus;
import com.example.comicvault.repository.PublisherRepository;
import com.example.comicvault.repository.SeriesRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeriesServiceTest {

  @Mock SeriesRepository repository;
  @Mock PublisherRepository publisherRepository;

  @InjectMocks SeriesService service;

  private static Publisher publisher(Long id, String name) {
    Publisher publisher = new Publisher(name, "España");
    ReflectionTestUtils.setField(publisher, "id", id);
    return publisher;
  }

  private static Series existing(Long id, Publisher publisher, String title) {
    Series series = new Series(publisher, title, SeriesStatus.ONGOING, 10);
    ReflectionTestUtils.setField(series, "id", id);
    return series;
  }

  @Test
  void creaLaSerieYDevuelveSuRespuesta() {
    given(publisherRepository.findById(1L)).willReturn(Optional.of(publisher(1L, "Norma")));
    given(repository.existsByPublisherIdAndTitleKey(1L, "berserk")).willReturn(false);
    given(repository.saveAndFlush(any(Series.class)))
        .willAnswer(
            invocation -> {
              Series saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 7L);
              return saved;
            });

    SeriesResponse response =
        service.create(new CreateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, 41));

    assertThat(response)
        .isEqualTo(new SeriesResponse(7L, "Berserk", 1L, "Norma", SeriesStatus.ONGOING, 41));
  }

  @Test
  void lanzaNotFoundAlCrearConUnaEditorialInexistente() {
    given(publisherRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.create(new CreateSeriesRequest("Berserk", 99L, SeriesStatus.ONGOING, 1)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Editorial con id 99 no encontrado");
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void lanzaConflictoAlCrearConUnTituloRepetidoEnLaEditorial() {
    given(publisherRepository.findById(1L)).willReturn(Optional.of(publisher(1L, "Norma")));
    given(repository.existsByPublisherIdAndTitleKey(1L, "berserk")).willReturn(true);

    assertThatThrownBy(
            () -> service.create(new CreateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, 1)))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Berserk");
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void comparaElTituloSinDistinguirMayusculas() {
    given(publisherRepository.findById(1L)).willReturn(Optional.of(publisher(1L, "Norma")));
    given(repository.existsByPublisherIdAndTitleKey(1L, "berserk")).willReturn(true);

    assertThatThrownBy(
            () -> service.create(new CreateSeriesRequest("BERSERK", 1L, SeriesStatus.ONGOING, 1)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void traduceLaViolacionDeLaRestriccionUnicaAConflictoAlCrear() {
    given(publisherRepository.findById(1L)).willReturn(Optional.of(publisher(1L, "Norma")));
    given(repository.existsByPublisherIdAndTitleKey(1L, "berserk")).willReturn(false);
    given(publisherRepository.existsById(1L)).willReturn(true);
    given(repository.saveAndFlush(any(Series.class)))
        .willThrow(new DataIntegrityViolationException("uk_series_publisher_title_key"));

    assertThatThrownBy(
            () -> service.create(new CreateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, 1)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void devuelveNotFoundSiLaEditorialDesapareceDuranteElAlta() {
    given(publisherRepository.findById(1L)).willReturn(Optional.of(publisher(1L, "Norma")));
    given(repository.existsByPublisherIdAndTitleKey(1L, "berserk")).willReturn(false);
    given(publisherRepository.existsById(1L)).willReturn(false);
    given(repository.saveAndFlush(any(Series.class)))
        .willThrow(new DataIntegrityViolationException("fk_series_publisher"));

    assertThatThrownBy(
            () -> service.create(new CreateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, 1)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Editorial con id 1 no encontrado");
  }

  @Test
  void devuelveLaSeriePorId() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));

    assertThat(service.get(3L))
        .isEqualTo(new SeriesResponse(3L, "Monster", 1L, "Norma", SeriesStatus.ONGOING, 10));
  }

  @Test
  void lanzaNotFoundAlPedirUnaSerieInexistente() {
    given(repository.findWithPublisherById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Serie con id 99 no encontrado");
  }

  @Test
  void actualizaTodosLosCampos() {
    Publisher norma = publisher(1L, "Norma");
    Publisher panini = publisher(2L, "Panini");
    Series series = existing(3L, norma, "Monster");
    given(repository.findWithPublisherById(3L)).willReturn(Optional.of(series));
    given(publisherRepository.findById(2L)).willReturn(Optional.of(panini));
    given(repository.existsByPublisherIdAndTitleKeyAndIdNot(2L, "batman", 3L)).willReturn(false);

    SeriesResponse response =
        service.update(3L, new UpdateSeriesRequest("Batman", 2L, SeriesStatus.FINISHED, 5));

    assertThat(response)
        .isEqualTo(new SeriesResponse(3L, "Batman", 2L, "Panini", SeriesStatus.FINISHED, 5));
    verify(repository).flush();
  }

  @Test
  void permiteVaciarTotalVolumesConPut() {
    Publisher norma = publisher(1L, "Norma");
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, norma, "Monster")));
    given(publisherRepository.findById(1L)).willReturn(Optional.of(norma));

    SeriesResponse response =
        service.update(3L, new UpdateSeriesRequest("Monster", 1L, SeriesStatus.ONGOING, null));

    assertThat(response.totalVolumes()).isNull();
  }

  @Test
  void lanzaNotFoundAlActualizarUnaSerieInexistente() {
    given(repository.findWithPublisherById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.update(99L, new UpdateSeriesRequest("X", 1L, SeriesStatus.ONGOING, null)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Serie con id 99 no encontrado");
  }

  @Test
  void lanzaNotFoundAlActualizarConUnaEditorialInexistente() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));
    given(publisherRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.update(3L, new UpdateSeriesRequest("X", 99L, SeriesStatus.ONGOING, null)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Editorial con id 99 no encontrado");
  }

  @Test
  void lanzaConflictoAlActualizarConElTituloDeOtraSerie() {
    Publisher norma = publisher(1L, "Norma");
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, norma, "Monster")));
    given(publisherRepository.findById(1L)).willReturn(Optional.of(norma));
    given(repository.existsByPublisherIdAndTitleKeyAndIdNot(1L, "berserk", 3L)).willReturn(true);

    assertThatThrownBy(
            () ->
                service.update(
                    3L, new UpdateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, null)))
        .isInstanceOf(ConflictException.class);
    verify(repository, never()).flush();
  }

  @Test
  void traduceLaViolacionDeLaRestriccionUnicaAConflictoAlActualizar() {
    Publisher norma = publisher(1L, "Norma");
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, norma, "Monster")));
    given(publisherRepository.findById(1L)).willReturn(Optional.of(norma));
    given(publisherRepository.existsById(1L)).willReturn(true);
    willThrow(new DataIntegrityViolationException("uk_series_publisher_title_key"))
        .given(repository)
        .flush();

    assertThatThrownBy(
            () ->
                service.update(
                    3L, new UpdateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, null)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void parcheaSoloLosCamposInformados() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));

    SeriesResponse response =
        service.patch(3L, new PatchSeriesRequest(null, null, SeriesStatus.FINISHED, null));

    assertThat(response)
        .isEqualTo(new SeriesResponse(3L, "Monster", 1L, "Norma", SeriesStatus.FINISHED, 10));
  }

  @Test
  void noCompruebaUnicidadSiNoCambiaTituloNiEditorial() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));

    service.patch(3L, new PatchSeriesRequest(null, null, null, 20));

    verify(repository, never()).existsByPublisherIdAndTitleKeyAndIdNot(any(), any(), any());
  }

  @Test
  void compruebaUnicidadConLaNuevaEditorialYElTituloActual() {
    Publisher panini = publisher(2L, "Panini");
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));
    given(publisherRepository.findById(2L)).willReturn(Optional.of(panini));
    given(repository.existsByPublisherIdAndTitleKeyAndIdNot(2L, "monster", 3L)).willReturn(true);

    assertThatThrownBy(() -> service.patch(3L, new PatchSeriesRequest(null, 2L, null, null)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void lanzaNotFoundAlParchearConUnaEditorialInexistente() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));
    given(publisherRepository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.patch(3L, new PatchSeriesRequest(null, 99L, null, null)))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void lanzaConflictoAlParchearConUnTituloRepetido() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));
    given(repository.existsByPublisherIdAndTitleKeyAndIdNot(1L, "berserk", 3L)).willReturn(true);

    assertThatThrownBy(() -> service.patch(3L, new PatchSeriesRequest("Berserk", null, null, null)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void traduceLaViolacionDeLaRestriccionUnicaAConflictoAlParchear() {
    given(repository.findWithPublisherById(3L))
        .willReturn(Optional.of(existing(3L, publisher(1L, "Norma"), "Monster")));
    given(publisherRepository.existsById(1L)).willReturn(true);
    willThrow(new DataIntegrityViolationException("uk_series_publisher_title_key"))
        .given(repository)
        .flush();

    assertThatThrownBy(() -> service.patch(3L, new PatchSeriesRequest("Berserk", null, null, null)))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void lanzaNotFoundAlParchearUnaSerieInexistente() {
    given(repository.findWithPublisherById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.patch(99L, new PatchSeriesRequest(null, null, null, 1)))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void borraLaSerieExistente() {
    Series series = existing(3L, publisher(1L, "Norma"), "Monster");
    given(repository.findWithPublisherById(3L)).willReturn(Optional.of(series));

    service.delete(3L);

    verify(repository).delete(series);
  }

  @Test
  void lanzaNotFoundAlBorrarUnaSerieInexistente() {
    given(repository.findWithPublisherById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).delete(any());
  }

  @Test
  void listaLasSeriesMapeandoLaEditorial() {
    PageRequest pageable = PageRequest.of(0, 20);
    given(repository.findAll(pageable))
        .willReturn(
            new PageImpl<>(List.of(existing(3L, publisher(1L, "Norma"), "Monster")), pageable, 1));

    var page = service.list(pageable);

    assertThat(page.getContent())
        .containsExactly(new SeriesResponse(3L, "Monster", 1L, "Norma", SeriesStatus.ONGOING, 10));
  }
}
