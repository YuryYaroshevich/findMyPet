package com.yy.petfinder.testfactory;

import com.yy.petfinder.model.User;
import org.bson.types.ObjectId;

public class UserFactory {
  public static User.UserBuilder userBuilderWithDefaults() {
    final String email = "abc@email.com";
    final String password = "1234";
    final String phone = "+375296666666";
    final String id = new ObjectId().toHexString();
    return User.builder().id(id).email(email).phone(phone).password(password);
  }
}
