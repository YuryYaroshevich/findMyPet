package com.yy.petfinder.persistence;

import com.yy.petfinder.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {
  Mono<User> findByEmail(String email);
}
