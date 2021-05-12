package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailMessageData {
  @NonNull private String text;
  @NonNull private String subject;
}
