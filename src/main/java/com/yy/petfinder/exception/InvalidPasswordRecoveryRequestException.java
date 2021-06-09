package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordRecoveryRequestException extends BaseException {
  private static final String MSG = "Password recovery request contains invalid token";
  private static final int CODE = 100007;

  public InvalidPasswordRecoveryRequestException() {
    super(MSG, HttpStatus.UNAUTHORIZED, CODE);
  }
}
