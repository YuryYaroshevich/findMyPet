package com.yy.petfinder.testfactory;

import com.yy.petfinder.model.User;
import com.yy.petfinder.rest.model.Messenger;
import java.util.List;
import org.bson.types.ObjectId;

public class UserFactory {
  public static User.UserBuilder userBuilderWithDefaults() {
    final String email = "abc@email.com";
    final String password = "1234";
    final String phone = "+375296666666";
    final String id = new ObjectId().toHexString();
    final List<Messenger> messengers = List.of(Messenger.TELEGRAM, Messenger.VIBER);
    return User.builder()
        .id(id)
        .email(email)
        .phone(phone)
        .password(password)
        .messengers(messengers);
  }
}
