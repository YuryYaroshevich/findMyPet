package com.yy.petfinder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum PetAdState {
  FOUND_BY_APP("found_by_app"),
  FOUND_BY_USER("found_by_user");

  private final String value;

  PetAdState(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator
  public static PetAdState of(final String petAdStateValue) {
    return Arrays.asList(PetAdState.values()).stream()
        .filter(petAdState -> petAdState.value().equals(petAdStateValue))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown pet ad state: " + petAdStateValue));
  }
}
