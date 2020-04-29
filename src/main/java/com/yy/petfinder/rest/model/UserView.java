package com.yy.petfinder.rest.model;

import java.util.Objects;

public class UserView {
  private String uuid;
  private String email;
  private String phone;

  public UserView(String uuid, String email, String phone) {
    this.uuid = uuid;
    this.email = email;
    this.phone = phone;
  }

  public String getUuid() {
    return uuid;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserView userView = (UserView) o;
    return uuid.equals(userView.uuid)
        && email.equals(userView.email)
        && phone.equals(userView.phone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, email, phone);
  }
}
