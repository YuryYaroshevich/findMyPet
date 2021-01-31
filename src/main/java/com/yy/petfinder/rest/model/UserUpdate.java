package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserUpdate {
  private String phone;
  private List<Messenger> messengers;
  private PasswordUpdate passwordUpdate;
}
