package com.yy.petfinder.rest.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Paging {
  private String nextPageToken;
  @Builder.Default private int pageSize = 20;
}
