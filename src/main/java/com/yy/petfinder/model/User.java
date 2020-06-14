package com.yy.petfinder.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Value
@Builder(toBuilder = true)
public class User {
  @Id @NonNull private String id;

  @NonNull private String email;
  @NonNull private String password;
  @NonNull private String phone;
}
