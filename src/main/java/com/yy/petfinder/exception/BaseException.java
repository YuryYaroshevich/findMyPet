package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
  private final HttpStatus status;
  private final int code;

  public BaseException(final String message, final HttpStatus status, final int code) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public int getCode() {
    return code;
  }
}
