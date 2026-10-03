package com.example.comicvault.common.error;

/**
 * Se lanza cuando la operación choca con el estado actual (duplicados, etc.). Se traduce a HTTP
 * 409.
 */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
