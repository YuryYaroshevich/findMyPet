package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends BaseException {
  private static final int CODE = 100002;

  public TokenException() {
    super("Invalid token provided", HttpStatus.UNAUTHORIZED, CODE);
  }
}
