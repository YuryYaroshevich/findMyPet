package com.yy.petfinder.model;

import java.util.Arrays;
import java.util.Optional;

public enum OAuth2Provider {
  GOOGLE("Google"),
  FACEBOOK("Facebook"),
  VK("VK");

  private final String name;

  OAuth2Provider(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static Optional<OAuth2Provider> of(final String value) {
    return Arrays.stream(values())
        .filter(provider -> provider.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
