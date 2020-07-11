package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class PetAdNotFoundException extends BaseException {
  private static final String MSG_TMPL = "PetAd with provided id not found: id=%s";

  public PetAdNotFoundException(final String petAdId) {
    super(String.format(MSG_TMPL, petAdId), HttpStatus.NOT_FOUND);
  }
}
