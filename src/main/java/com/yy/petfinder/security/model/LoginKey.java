package com.yy.petfinder.security.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginKey {
  private String id;
  private String key;
}
