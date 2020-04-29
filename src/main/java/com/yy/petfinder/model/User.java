package com.yy.petfinder.model;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
  @Id private final ObjectId id;

  @Indexed(unique = true, background = true)
  private final String uuid;

  private final String email;

  private final String password;
  private final String phone;

  public User(ObjectId id, String uuid, String email, String password, String phone) {
    this.id = id;
    this.uuid = uuid;
    this.email = email;
    this.password = password;
    this.phone = phone;
  }

  public ObjectId getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getPhone() {
    return phone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id.equals(user.id)
        && uuid.equals(user.uuid)
        && email.equals(user.email)
        && password.equals(user.password)
        && phone.equals(user.phone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uuid, email, password, phone);
  }
}
