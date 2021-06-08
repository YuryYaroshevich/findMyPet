package com.yy.petfinder.exception;

import com.yy.petfinder.model.OAuth2Provider;
import org.springframework.http.HttpStatus;

public class UserPasswordUpdateException extends BaseException {
  private static final String MSG_TMPL = "User was created with oauth2 provider(%s)";
  private static final int CODE = 100000;

  public UserPasswordUpdateException(final OAuth2Provider oAuth2Provider) {
    super(String.format(MSG_TMPL, oAuth2Provider.getName()),
      HttpStatus.BAD_REQUEST, CODE);
  }
}
