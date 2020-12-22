package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class CreateUser {
  @NonNull private String email;
  @NonNull private String phone;
  @NonNull private String password;
  private List<Messenger> messengers;
}
