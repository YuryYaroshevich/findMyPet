package com.yy.petfinder.security.service;

import com.yy.petfinder.persistence.UserRepository;
import java.util.List;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDetailsService implements ReactiveUserDetailsService {
  private final UserRepository userRepository;

  public UserDetailsService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<UserDetails> findByUsername(String userId) {
    return userRepository
        .findById(userId)
        .map(user -> new User(user.getId(), user.getPassword(), List.of()));
  }
}
