package com.yy.petfinder.model;

import com.yy.petfinder.rest.model.Messenger;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Value
@Builder(toBuilder = true)
public class User {
  @Id @NonNull private String id;

  @Indexed(unique = true)
  @NonNull
  private String email;

  private String password;
  private String phone;
  private List<Messenger> messengers;

  private OAuth2Provider oAuth2Provider;
}
