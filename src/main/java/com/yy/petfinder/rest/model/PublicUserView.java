package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class PublicUserView {
  @NonNull private String id;
  @NonNull private String phone;
}
