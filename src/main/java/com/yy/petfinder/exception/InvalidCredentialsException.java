package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
  public InvalidCredentialsException() {
    super("Invalid credentials", HttpStatus.UNAUTHORIZED);
  }
}
