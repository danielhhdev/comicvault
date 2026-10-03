package com.example.comicvault.common.error;

/** Se lanza cuando un recurso pedido por id no existe. Se traduce a HTTP 404. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String resource, Object id) {
    super("%s con id %s no encontrado".formatted(resource, id));
  }
}
