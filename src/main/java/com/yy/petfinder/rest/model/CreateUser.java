package com.yy.petfinder.rest.model;

import java.util.Objects;

public class CreateUser {
  private String email;
  private String phone;
  private String password;

  public CreateUser(String email, String phone, String password) {
    this.email = email;
    this.phone = phone;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateUser that = (CreateUser) o;
    return email.equals(that.email) && phone.equals(that.phone) && password.equals(that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, phone, password);
  }
}
