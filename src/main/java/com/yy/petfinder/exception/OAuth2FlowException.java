package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class OAuth2FlowException extends BaseException {
  private static final String MSG = "Error happened during OAuth2 authentication workflow";
  private static final int CODE = 100005;

  public OAuth2FlowException() {
    super(MSG, HttpStatus.UNAUTHORIZED, CODE);
  }
}
