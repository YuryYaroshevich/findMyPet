package com.yy.petfinder.rest;

import static com.yy.petfinder.util.UserIdRetriever.userIdFromContext;

import com.yy.petfinder.rest.model.PasswordUpdateEmail;
import com.yy.petfinder.rest.model.PasswordUpdateRequest;
import com.yy.petfinder.rest.model.PrivateUserView;
import com.yy.petfinder.rest.model.PublicUserView;
import com.yy.petfinder.rest.model.UserUpdate;
import com.yy.petfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/users")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/private")
  public Mono<PrivateUserView> getPrivateUserView() {
    return userIdFromContext().flatMap(userId -> userService.getUser(userId));
  }

  @GetMapping("/{id}/public")
  public Mono<PublicUserView> getPublicUserView(@PathVariable("id") final String id) {
    return userService.getUser(id).map(u -> toPublicView(u));
  }

  @PutMapping
  public Mono<PrivateUserView> updateUser(@RequestBody UserUpdate userUpdate) {
    return userIdFromContext().flatMap(userId -> userService.updateUser(userId, userUpdate));
  }

  @PutMapping("/newPassword")
  public Mono<Void> updateUserWithNewPassword(
      @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
    return userService.setNewPassword(passwordUpdateRequest).then();
  }

  @PostMapping("/newPasswordEmail")
  public Mono<Void> sendNewPasswordEmail(@RequestBody PasswordUpdateEmail passwordUpdateEmail) {
    return userService.initiatePasswordUpdate(passwordUpdateEmail).then();
  }

  private static PublicUserView toPublicView(final PrivateUserView privateUserView) {
    return PublicUserView.builder()
        .id(privateUserView.getId())
        .phone(privateUserView.getPhone())
        .messengers(privateUserView.getMessengers())
        .build();
  }
}
