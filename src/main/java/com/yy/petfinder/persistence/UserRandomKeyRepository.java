package com.yy.petfinder.persistence;

import com.yy.petfinder.model.UserRandomKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRandomKeyRepository extends ReactiveCrudRepository<UserRandomKey, String> {}
