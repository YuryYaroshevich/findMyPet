package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class UserPasswordUpdateException extends BaseException {
  public UserPasswordUpdateException() {
    super("User was created with oauth2 provider", HttpStatus.BAD_REQUEST);
  }
}
