package com.example.comicvault.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.comicvault.common.error.ConflictException;
import com.example.comicvault.common.error.NotFoundException;
import com.example.comicvault.publisher.dto.CreatePublisherRequest;
import com.example.comicvault.publisher.dto.PublisherResponse;
import com.example.comicvault.publisher.dto.UpdatePublisherRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublisherController.class)
class PublisherControllerTest {

  private static final String VALID_BODY = "{\"name\":\"Panini\",\"country\":\"Italia\"}";

  @Autowired MockMvc mockMvc;

  @MockitoBean PublisherService service;

  @Test
  void listaLasEditorialesPaginadas() throws Exception {
    given(service.list(any()))
        .willReturn(
            new PageImpl<>(
                List.of(new PublisherResponse(1L, "Panini", "Italia")), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/publishers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Panini"))
        .andExpect(jsonPath("$.page.totalElements").value(1));
  }

  @Test
  void devuelveLaEditorialPorId() throws Exception {
    given(service.get(1L)).willReturn(new PublisherResponse(1L, "Panini", "Italia"));

    mockMvc
        .perform(get("/api/v1/publishers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.country").value("Italia"));
  }

  @Test
  void devuelve404CuandoLaEditorialNoExiste() throws Exception {
    given(service.get(99L)).willThrow(new NotFoundException("Editorial", 99L));

    mockMvc
        .perform(get("/api/v1/publishers/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Editorial con id 99 no encontrado"));
  }

  @Test
  void creaLaEditorialConLocation() throws Exception {
    given(service.create(new CreatePublisherRequest("Panini", "Italia")))
        .willReturn(new PublisherResponse(5L, "Panini", "Italia"));

    mockMvc
        .perform(
            post("/api/v1/publishers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/publishers/5"))
        .andExpect(jsonPath("$.id").value(5));
  }

  @Test
  void devuelve400CuandoElNombreEstaEnBlanco() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/publishers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  \",\"country\":\"Italia\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void devuelve400CuandoElNombreSuperaLaLongitudMaxima() throws Exception {
    String longName = "x".repeat(121);

    mockMvc
        .perform(
            post("/api/v1/publishers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + longName + "\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void devuelve409CuandoElNombreYaExiste() throws Exception {
    given(service.create(any())).willThrow(new ConflictException("Ya existe esa editorial"));

    mockMvc
        .perform(
            post("/api/v1/publishers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Ya existe esa editorial"));
  }

  @Test
  void actualizaLaEditorial() throws Exception {
    given(service.update(1L, new UpdatePublisherRequest("Panini", "Italia")))
        .willReturn(new PublisherResponse(1L, "Panini", "Italia"));

    mockMvc
        .perform(
            put("/api/v1/publishers/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Panini"));
  }

  @Test
  void devuelve404AlActualizarUnaEditorialInexistente() throws Exception {
    given(service.update(any(), any())).willThrow(new NotFoundException("Editorial", 99L));

    mockMvc
        .perform(
            put("/api/v1/publishers/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isNotFound());
  }

  @Test
  void devuelve400AlActualizarConUnNombreEnBlanco() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/publishers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void borraLaEditorialYDevuelve204() throws Exception {
    mockMvc.perform(delete("/api/v1/publishers/1")).andExpect(status().isNoContent());

    verify(service).delete(1L);
  }

  @Test
  void devuelve404AlBorrarUnaEditorialInexistente() throws Exception {
    doThrow(new NotFoundException("Editorial", 99L)).when(service).delete(99L);

    mockMvc.perform(delete("/api/v1/publishers/99")).andExpect(status().isNotFound());
  }
}
