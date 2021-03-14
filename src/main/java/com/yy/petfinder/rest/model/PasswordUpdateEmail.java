package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PasswordUpdateEmail {
  @NonNull private String email;
  @NonNull private String frontendHost;
}
