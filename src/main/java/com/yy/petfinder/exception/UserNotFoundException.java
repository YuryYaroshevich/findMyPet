package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
  private static final String ID_MSG_TMPL = "User with provided id not found: id=%s";
  private static final String EMAIL_MSG_TMPL = "User with provided email not found: email=%s";

  private UserNotFoundException(final String msg) {
    super(msg, HttpStatus.NOT_FOUND);
  }

  public static UserNotFoundException withId(final String userId) {
    return new UserNotFoundException(String.format(ID_MSG_TMPL, userId));
  }

  public static UserNotFoundException withEmail(final String email) {
    return new UserNotFoundException(String.format(EMAIL_MSG_TMPL, email));
  }
}
