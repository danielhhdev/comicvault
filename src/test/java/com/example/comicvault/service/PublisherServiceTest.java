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
import com.example.comicvault.dto.CreatePublisherRequest;
import com.example.comicvault.dto.PublisherResponse;
import com.example.comicvault.dto.UpdatePublisherRequest;
import com.example.comicvault.entity.Publisher;
import com.example.comicvault.repository.PublisherRepository;
import com.example.comicvault.repository.SeriesRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

  @Mock PublisherRepository repository;
  @Mock SeriesRepository seriesRepository;

  @InjectMocks PublisherService service;

  @Test
  void creaLaEditorialYDevuelveSuRespuesta() {
    given(repository.existsByNameIgnoreCase("Panini")).willReturn(false);
    given(repository.saveAndFlush(any(Publisher.class)))
        .willAnswer(
            invocation -> {
              Publisher saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 1L);
              return saved;
            });

    PublisherResponse response = service.create(new CreatePublisherRequest("Panini", "Italia"));

    assertThat(response).isEqualTo(new PublisherResponse(1L, "Panini", "Italia"));
  }

  @Test
  void lanzaConflictoAlCrearConUnNombreRepetido() {
    given(repository.existsByNameIgnoreCase("Panini")).willReturn(true);

    assertThatThrownBy(() -> service.create(new CreatePublisherRequest("Panini", "Italia")))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Panini");
    verify(repository, never()).save(any());
  }

  @Test
  void lanzaConflictoAlCrearConUnNombreQueSoloDifiereEnMayusculas() {
    given(repository.existsByNameIgnoreCase("panini")).willReturn(true);

    assertThatThrownBy(() -> service.create(new CreatePublisherRequest("panini", "Italia")))
        .isInstanceOf(ConflictException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void lanzaNotFoundAlPedirUnaEditorialInexistente() {
    given(repository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Editorial con id 99 no encontrado");
  }

  @Test
  void actualizaLosCamposDeLaEditorial() {
    Publisher publisher = existing(1L, "Norma", "España");
    given(repository.findById(1L)).willReturn(Optional.of(publisher));
    given(repository.existsByNameIgnoreCaseAndIdNot("Norma Editorial", 1L)).willReturn(false);

    PublisherResponse response =
        service.update(1L, new UpdatePublisherRequest("Norma Editorial", "Francia"));

    assertThat(response).isEqualTo(new PublisherResponse(1L, "Norma Editorial", "Francia"));
  }

  @Test
  void lanzaConflictoAlActualizarConElNombreDeOtraEditorial() {
    given(repository.findById(1L)).willReturn(Optional.of(existing(1L, "Norma", "España")));
    given(repository.existsByNameIgnoreCaseAndIdNot("ECC", 1L)).willReturn(true);

    assertThatThrownBy(() -> service.update(1L, new UpdatePublisherRequest("ECC", "España")))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void lanzaNotFoundAlActualizarUnaEditorialInexistente() {
    given(repository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(99L, new UpdatePublisherRequest("ECC", "España")))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void borraLaEditorialExistente() {
    Publisher publisher = existing(1L, "Norma", "España");
    given(repository.findById(1L)).willReturn(Optional.of(publisher));

    service.delete(1L);

    verify(repository).delete(publisher);
    verify(repository).flush();
  }

  @Test
  void lanzaConflictoAlBorrarUnaEditorialConSeries() {
    given(repository.findById(1L)).willReturn(Optional.of(existing(1L, "Norma", "España")));
    given(seriesRepository.existsByPublisherId(1L)).willReturn(true);

    assertThatThrownBy(() -> service.delete(1L))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("tiene series");
    verify(repository, never()).delete(any());
  }

  @Test
  void traduceLaViolacionDeLaClaveForaneaAConflictoAlBorrar() {
    given(repository.findById(1L)).willReturn(Optional.of(existing(1L, "Norma", "España")));
    willThrow(new DataIntegrityViolationException("fk_series_publisher")).given(repository).flush();

    assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ConflictException.class);
  }

  @Test
  void lanzaNotFoundAlBorrarUnaEditorialInexistente() {
    given(repository.findById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).delete(any());
  }

  @Test
  void traduceLaViolacionDeLaRestriccionUnicaAConflictoAlCrear() {
    given(repository.existsByNameIgnoreCase("Panini")).willReturn(false);
    given(repository.saveAndFlush(any(Publisher.class)))
        .willThrow(new DataIntegrityViolationException("uk_publishers_name"));

    assertThatThrownBy(() -> service.create(new CreatePublisherRequest("Panini", "Italia")))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Panini");
  }

  @Test
  void traduceLaViolacionDeLaRestriccionUnicaAConflictoAlActualizar() {
    given(repository.findById(1L)).willReturn(Optional.of(existing(1L, "Norma", "España")));
    given(repository.existsByNameIgnoreCaseAndIdNot("ECC", 1L)).willReturn(false);
    willThrow(new DataIntegrityViolationException("uk_publishers_name")).given(repository).flush();

    assertThatThrownBy(() -> service.update(1L, new UpdatePublisherRequest("ECC", "España")))
        .isInstanceOf(ConflictException.class);
  }

  private Publisher existing(Long id, String name, String country) {
    Publisher publisher = new Publisher(name, country);
    ReflectionTestUtils.setField(publisher, "id", id);
    return publisher;
  }
}
