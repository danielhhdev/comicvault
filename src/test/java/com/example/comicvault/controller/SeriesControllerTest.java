package com.example.comicvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.dto.CreateSeriesRequest;
import com.example.comicvault.dto.SeriesResponse;
import com.example.comicvault.entity.SeriesStatus;
import com.example.comicvault.service.SeriesService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SeriesController.class)
class SeriesControllerTest {

  private static final String VALID_BODY =
      "{\"title\":\"Berserk\",\"publisherId\":1,\"status\":\"ONGOING\",\"totalVolumes\":41}";

  @Autowired MockMvc mockMvc;

  @MockitoBean SeriesService service;

  private static SeriesResponse berserk(Long id) {
    return new SeriesResponse(id, "Berserk", 1L, "Norma", SeriesStatus.ONGOING, 41);
  }

  private void postBody(String body, int expectedStatus) throws Exception {
    mockMvc
        .perform(post("/api/v1/series").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().is(expectedStatus));
  }

  @Test
  void listaLasSeriesPaginadas() throws Exception {
    given(service.list(any()))
        .willReturn(new PageImpl<>(List.of(berserk(1L)), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/series"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Berserk"))
        .andExpect(jsonPath("$.content[0].publisherName").value("Norma"))
        .andExpect(jsonPath("$.page.totalElements").value(1));
  }

  @Test
  void limitaElTamanoDePaginaA50YOrdenaPorTituloSiNoSeIndicaOrden() throws Exception {
    given(service.list(any())).willReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/api/v1/series?size=1000")).andExpect(status().isOk());

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(service).list(captor.capture());
    assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("title"));
  }

  @Test
  void devuelveLaSeriePorId() throws Exception {
    given(service.get(1L)).willReturn(berserk(1L));

    mockMvc
        .perform(get("/api/v1/series/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.publisherId").value(1))
        .andExpect(jsonPath("$.status").value("ONGOING"));
  }

  @Test
  void devuelve404CuandoLaSerieNoExiste() throws Exception {
    given(service.get(99L)).willThrow(new NotFoundException("Serie", 99L));

    mockMvc
        .perform(get("/api/v1/series/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Serie con id 99 no encontrado"));
  }

  @Test
  void devuelve400CuandoElIdNoEsNumerico() throws Exception {
    mockMvc.perform(get("/api/v1/series/abc")).andExpect(status().isBadRequest());
  }

  @Test
  void creaLaSerieConLocation() throws Exception {
    given(service.create(new CreateSeriesRequest("Berserk", 1L, SeriesStatus.ONGOING, 41)))
        .willReturn(berserk(5L));

    mockMvc
        .perform(post("/api/v1/series").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/series/5"))
        .andExpect(jsonPath("$.id").value(5));
  }

  @Test
  void devuelve400CuandoElTituloEstaEnBlanco() throws Exception {
    postBody("{\"title\":\"  \",\"publisherId\":1,\"status\":\"ONGOING\"}", 400);
  }

  @Test
  void devuelve400CuandoElTituloSuperaLaLongitudMaxima() throws Exception {
    postBody(
        "{\"title\":\"%s\",\"publisherId\":1,\"status\":\"ONGOING\"}".formatted("a".repeat(201)),
        400);
  }

  @Test
  void devuelve400CuandoFaltaLaEditorial() throws Exception {
    postBody("{\"title\":\"Berserk\",\"status\":\"ONGOING\"}", 400);
  }

  @Test
  void devuelve400CuandoFaltaElEstado() throws Exception {
    postBody("{\"title\":\"Berserk\",\"publisherId\":1}", 400);
  }

  @Test
  void devuelve400ConUnEstadoDesconocido() throws Exception {
    postBody("{\"title\":\"Berserk\",\"publisherId\":1,\"status\":\"FOO\"}", 400);
  }

  @Test
  void devuelve400ConTotalVolumesCero() throws Exception {
    postBody(
        "{\"title\":\"Berserk\",\"publisherId\":1,\"status\":\"ONGOING\",\"totalVolumes\":0}", 400);
  }

  @Test
  void devuelve404AlCrearConUnaEditorialInexistente() throws Exception {
    given(service.create(any())).willThrow(new NotFoundException("Editorial", 1L));

    postBody(VALID_BODY, 404);
  }

  @Test
  void devuelve409CuandoElTituloYaExisteEnLaEditorial() throws Exception {
    given(service.create(any())).willThrow(new ConflictException("Ya existe una serie"));

    postBody(VALID_BODY, 409);
  }
}
