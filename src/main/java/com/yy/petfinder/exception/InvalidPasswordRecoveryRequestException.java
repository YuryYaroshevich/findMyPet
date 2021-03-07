package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordRecoveryRequestException extends BaseException {
  public InvalidPasswordRecoveryRequestException() {
    super("Password recovery request contains invalid token", HttpStatus.UNAUTHORIZED);
  }
}
