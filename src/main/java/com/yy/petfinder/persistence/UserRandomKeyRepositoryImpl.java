package com.yy.petfinder.persistence;

import com.yy.petfinder.model.UserRandomKey;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

public class UserRandomKeyRepositoryImpl implements UserRandomKeyRepositoryCustom {
  private static final String ID_FIELD = "_id";
  private static final String RANDOM_KEY_FIELD = "randomKey";

  private final ReactiveMongoTemplate mongoTemplate;

  public UserRandomKeyRepositoryImpl(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Mono<UserRandomKey> findAndRemove(String id, String key) {
    final Criteria criteria = Criteria.where(ID_FIELD).is(id).and(RANDOM_KEY_FIELD).is(key);
    return mongoTemplate.findAndRemove(new Query(criteria), UserRandomKey.class);
  }
}
