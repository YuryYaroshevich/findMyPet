package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class PasswordUpdate {
  @NonNull private String newPassword;
  @NonNull private String oldPassword;
}
