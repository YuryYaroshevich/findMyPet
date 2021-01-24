package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class InvalidSearchAreaException extends BaseException {
  public InvalidSearchAreaException() {
    super("Coordinates specified in search area form invalid polygon", HttpStatus.BAD_REQUEST);
  }
}
