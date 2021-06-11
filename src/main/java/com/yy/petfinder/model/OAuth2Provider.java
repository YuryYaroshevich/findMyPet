package com.yy.petfinder.model;

import java.util.Arrays;
import java.util.Optional;

public enum OAuth2Provider {
  GOOGLE("Google", "sub"),
  FACEBOOK("Facebook", "id"),
  VK("VK", "id");

  private final String name;
  private final String nameAttributeKey;

  OAuth2Provider(final String name, final String nameAttributeKey) {
    this.name = name;
    this.nameAttributeKey = nameAttributeKey;
  }

  public String getName() {
    return name;
  }

  public String getNameAttributeKey() {
    return nameAttributeKey;
  }

  public static Optional<OAuth2Provider> of(final String value) {
    return Arrays.stream(values())
        .filter(provider -> provider.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
