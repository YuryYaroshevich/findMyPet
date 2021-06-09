package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class InvalidSearchAreaException extends BaseException {
  private static final String MSG = "Coordinates specified in search area form invalid polygon";
  private static final int CODE = 100006;

  public InvalidSearchAreaException() {
    super(MSG, HttpStatus.BAD_REQUEST, CODE);
  }
}
