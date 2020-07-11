package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
  private HttpStatus status;

  public BaseException(final String message, final HttpStatus status) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
