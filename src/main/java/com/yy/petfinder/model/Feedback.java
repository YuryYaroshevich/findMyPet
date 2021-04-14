package com.yy.petfinder.model;

import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@Document
public class Feedback {
  @Id @NonNull private String id;
  @NonNull private String userId;
  @NonNull private String text;
  @Indexed @NonNull private Instant createdAt;
}
