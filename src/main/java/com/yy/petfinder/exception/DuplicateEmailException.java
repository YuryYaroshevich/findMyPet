package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BaseException {
  private static final String MSG_TMPL = "User with such email already exists: email=%s";
  private static final int CODE = 100009;

  public DuplicateEmailException(final String email) {
    super(String.format(MSG_TMPL, email), HttpStatus.CONFLICT, CODE);
  }
}
