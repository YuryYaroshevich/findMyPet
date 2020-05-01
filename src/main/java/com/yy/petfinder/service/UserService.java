package com.yy.petfinder.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.UserView;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<UserView> getUser(final String uuid) {
    final Mono<User> user = userRepository.findByUuid(uuid);
    final Mono<UserView> userView = user.map(this::userToView);
    return userView;
  }

  public Mono<UserView> createUser(CreateUser createUser) {
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();
    final User newUser =
        User.builder()
            .id(objectId)
            .uuid(uuid)
            .email(createUser.getEmail())
            .phone(createUser.getPhone())
            .password(createUser.getPassword())
            .build();

    final Mono<User> createdUser = userRepository.save(newUser);
    final Mono<UserView> userView = createdUser.map(this::userToView);
    return userView;
  }

  private UserView userToView(final User user) {
    return new UserView(user.getUuid(), user.getEmail(), user.getPhone());
  }
}
