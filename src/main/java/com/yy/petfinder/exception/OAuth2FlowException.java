package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class OAuth2FlowException extends BaseException {
  public OAuth2FlowException() {
    super("Error happened during OAuth2 authentication workflow", HttpStatus.UNAUTHORIZED);
  }
}
