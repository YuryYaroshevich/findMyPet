package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class Login {
  @NonNull private String email;
  @NonNull private String password;
}
