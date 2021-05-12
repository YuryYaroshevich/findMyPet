package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class SpotAdNotFoundException extends BaseException {
  private static final String MSG_TMPL = "SpotAd with provided id not found: id=%s";

  public SpotAdNotFoundException(final String id) {
    super(String.format(MSG_TMPL, id), HttpStatus.NOT_FOUND);
  }
}
