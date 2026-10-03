package com.example.comicvault.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.http.ProblemDetail;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void traduceNotFoundAProblemDetail404() {
    ProblemDetail problem = handler.handleNotFound(new NotFoundException("Series", 7L));

    assertThat(problem.getStatus()).isEqualTo(404);
    assertThat(problem.getDetail()).isEqualTo("Series con id 7 no encontrado");
  }

  @Test
  void traduceConflictAProblemDetail409() {
    ProblemDetail problem = handler.handleConflict(new ConflictException("Volumen duplicado"));

    assertThat(problem.getStatus()).isEqualTo(409);
    assertThat(problem.getDetail()).isEqualTo("Volumen duplicado");
  }

  @Test
  void traduceUnSortInvalidoAProblemDetail400() {
    var exception =
        new PropertyReferenceException(
            "foo", TypeInformation.of(String.class), java.util.List.of());

    ProblemDetail problem = handler.handleInvalidSort(exception);

    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getDetail()).isEqualTo("No se puede ordenar por 'foo'");
  }
}
