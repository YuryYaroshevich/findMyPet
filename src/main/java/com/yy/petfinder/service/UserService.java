package com.yy.petfinder.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.util.UUIDService;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final UUIDService uuidService;

  @Autowired
  public UserService(UserRepository userRepository, UUIDService uuidService) {
    this.userRepository = userRepository;
    this.uuidService = uuidService;
  }

  public Mono<User> getUser(final String email) {
    return userRepository.findByEmail(email);
  }

  public Mono<User> createUser(User user) {
    final UUID uuid = uuidService.generateUUIDFromBytes(user.getEmail().getBytes());
    user.setUuid(uuid.toString());
    return userRepository.save(user)
            .doOnEach(u ->
                    System.out.println("Created user: " + u + " " + LocalDateTime.now()));
  }
}
