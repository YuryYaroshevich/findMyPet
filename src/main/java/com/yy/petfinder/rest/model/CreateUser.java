package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class CreateUser {
  @NonNull private String email;
  @NonNull private String phone;
  @NonNull private String password;
}
