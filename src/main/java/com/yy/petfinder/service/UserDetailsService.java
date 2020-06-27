package com.yy.petfinder.service;

import com.yy.petfinder.persistence.UserRepository;
import java.util.List;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDetailsService implements ReactiveUserDetailsService {
  private UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String email) {
    return userRepository
        .findByEmail(email)
        .map(user -> new User(user.getEmail(), user.getPassword(), List.of()));
  }
}
