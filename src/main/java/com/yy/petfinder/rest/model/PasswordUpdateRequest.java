package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PasswordUpdateRequest {
  @NonNull private String userId;
  @NonNull private String key;
  @NonNull private String newPassword;
}
