package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
  private static final String MSG_TMPL = "User with provided id not found: id=%s";

  public UserNotFoundException(final String userId) {
    super(String.format(MSG_TMPL, userId), HttpStatus.NOT_FOUND);
  }
}
