package com.yy.petfinder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum PetType {
  DOG("dog"),
  CAT("cat"),
  BIRD("bird"),
  OTHER("other");

  private final String value;

  PetType(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return value;
  }

  @JsonCreator
  public static PetType of(final String petTypeValue) {
    return Arrays.asList(PetType.values()).stream()
        .filter(petType -> petType.equals(petTypeValue))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown pet type"));
  }
}
