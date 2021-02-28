package com.yy.petfinder.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Value
@Builder(toBuilder = true)
public class UserRandomKey {
  @Id @NonNull private String id;
  @NonNull private String randomKey;
}
