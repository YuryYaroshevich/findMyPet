package com.yy.petfinder.service;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<User> getUser(final String email) {
    return userRepository.findByEmail(email);
  }

  public Mono<User> createUser(User user) {
    return userRepository.save(user);
  }
}
