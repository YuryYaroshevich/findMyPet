package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends BaseException {
  public TokenException() {
    super("Invalid token provided", HttpStatus.UNAUTHORIZED);
  }
}
