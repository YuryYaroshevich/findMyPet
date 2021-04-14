package com.yy.petfinder.rest.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class FeedbackView {
  @NonNull private String text;
}
