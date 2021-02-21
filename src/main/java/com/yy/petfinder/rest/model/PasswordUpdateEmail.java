package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PasswordUpdateEmail {
  private String email;
  private String frontendHost;
}
