package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PrivateUserView {
  @NonNull private String id;
  @NonNull private String email;
  private String phone;
  private List<Messenger> messengers;
}
