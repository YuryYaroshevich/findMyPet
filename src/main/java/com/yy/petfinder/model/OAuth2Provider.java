package com.yy.petfinder.model;

public enum OAuth2Provider {
  GOOGLE("Google");

  private final String name;

  OAuth2Provider(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
