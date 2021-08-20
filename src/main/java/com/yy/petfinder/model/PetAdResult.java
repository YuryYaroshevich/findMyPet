package com.yy.petfinder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum PetAdResult {
  FOUND_BY_APP("found_by_app"),
  FOUND_BY_USER("found_by_user"),
  PET_AD_NOT_RELEVANT("not_relevant"),
  REMOVED_WITH_PROFILE("not_relevant");

  private final String value;

  PetAdResult(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator
  public static PetAdResult of(final String petAdStateValue) {
    return Arrays.asList(PetAdResult.values()).stream()
        .filter(petAdState -> petAdState.value().equals(petAdStateValue))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown pet ad state: " + petAdStateValue));
  }

  @Override
  public String toString() {
    return value;
  }
}
