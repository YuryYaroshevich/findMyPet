package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class UserView {
  @NonNull private String id;
  @NonNull private String email;
  @NonNull private String phone;
}
