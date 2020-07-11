package com.yy.petfinder.rest;

import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.Login;
import com.yy.petfinder.security.model.JWTToken;
import com.yy.petfinder.security.service.LoginService;
import com.yy.petfinder.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {
  private UserService userService;
  private LoginService loginService;

  public AuthController(UserService userService, LoginService loginService) {
    this.userService = userService;
    this.loginService = loginService;
  }

  @PostMapping(value = "/login")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<JWTToken> login(@RequestBody Login login) {
    return loginService.authenticate(login);
  }

  @PostMapping(value = "/signUp")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> createUser(@RequestBody CreateUser user) {
    return userService.createUser(user).then();
  }
}
