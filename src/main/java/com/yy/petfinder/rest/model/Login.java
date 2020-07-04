package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Login {
  @NonNull private String email;
  @NonNull private String password;
}
