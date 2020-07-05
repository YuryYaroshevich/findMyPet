package com.yy.petfinder.persistence;

import com.yy.petfinder.model.User;
import com.yy.petfinder.rest.model.UserUpdate;
import reactor.core.publisher.Mono;

public interface UserRepositoryCustom {
  Mono<User> findAndModify(UserUpdate userUpdate, String userId);
}
