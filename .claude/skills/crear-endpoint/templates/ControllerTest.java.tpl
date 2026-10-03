package com.example.comicvault.__recurso__;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.comicvault.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(__Recurso__Controller.class)
class __Recurso__ControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean __Recurso__Service service;

  @Test
  void devuelve404CuandoElRecursoNoExiste() throws Exception {
    given(service.get(99L)).willThrow(new NotFoundException("__Recurso__", 99L));

    mockMvc.perform(get("/api/v1/__recursos__/99")).andExpect(status().isNotFound());
  }
}
