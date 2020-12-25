package com.yy.petfinder.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Messenger {
  TELEGRAM("telegram"),
  VIBER("viber");

  private final String value;

  Messenger(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator
  public static Messenger of(final String messengerValue) {
    return Arrays.asList(Messenger.values()).stream()
        .filter(messenger -> messenger.value().equals(messengerValue))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown messenger type: " + messengerValue));
  }
}
