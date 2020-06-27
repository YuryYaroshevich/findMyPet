package com.yy.petfinder.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.UserView;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Mono<UserView> getUser(final String id) {
    final Mono<User> user = userRepository.findById(id);
    final Mono<UserView> userView = user.map(this::userToView);
    return userView;
  }

  public Mono<UserView> createUser(CreateUser createUser) {
    final String id = new ObjectId().toHexString();
    final String encodedPassword = passwordEncoder.encode(createUser.getPassword());
    final User newUser =
        User.builder()
            .id(id)
            .email(createUser.getEmail())
            .phone(createUser.getPhone())
            .password(encodedPassword)
            .build();

    final Mono<User> createdUser = userRepository.save(newUser);
    final Mono<UserView> userView = createdUser.map(this::userToView);
    return userView;
  }

  private UserView userToView(final User user) {
    return new UserView(user.getId(), user.getEmail(), user.getPhone());
  }
}
