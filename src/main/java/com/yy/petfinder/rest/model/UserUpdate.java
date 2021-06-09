package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Value
@Builder(toBuilder = true)
public class UserUpdate {
  private String phone;
  private List<Messenger> messengers;
  private PasswordUpdate passwordUpdate;

  public boolean isEmpty() {
    return StringUtils.isEmpty(phone)
        && CollectionUtils.isEmpty(messengers)
        && passwordUpdate == null;
  }
}
