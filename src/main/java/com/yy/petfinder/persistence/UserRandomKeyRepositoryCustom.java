package com.yy.petfinder.persistence;

import com.yy.petfinder.model.UserRandomKey;
import reactor.core.publisher.Mono;

public interface UserRandomKeyRepositoryCustom {
  Mono<UserRandomKey> findAndRemove(String id, String key);
}
