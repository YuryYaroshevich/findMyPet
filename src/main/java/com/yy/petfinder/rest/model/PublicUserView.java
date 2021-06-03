package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class PublicUserView {
  @NonNull private String id;
  private String phone;
  private List<Messenger> messengers;
}
