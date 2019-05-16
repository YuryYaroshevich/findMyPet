package com.yy.petfinder.model;

import java.util.Objects;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
  @Indexed(unique = true, background = true)
  private String uuid;

  @Indexed(unique = true, background = true)
  private String email;

  private String password;
  private String phone;

  public User(String uuid, String email, String phone) {
    this.uuid = uuid;
    this.email = email;
    this.phone = phone;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return uuid.equals(user.uuid)
        && email.equals(user.email)
        && Objects.equals(password, user.password)
        && Objects.equals(phone, user.phone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, email, password, phone);
  }
}
