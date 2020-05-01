package com.yy.petfinder.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Value
@Builder(toBuilder = true)
public class User {
  @Id @NonNull private ObjectId id;

  @Indexed(unique = true, background = true)
  @NonNull
  private String uuid;

  @NonNull private String email;
  @NonNull private String password;
  @NonNull private String phone;
}
