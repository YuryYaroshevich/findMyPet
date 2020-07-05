package com.yy.petfinder.persistence;

import com.yy.petfinder.model.User;
import com.yy.petfinder.rest.model.UserUpdate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

public class UserRepositoryImpl implements UserRepositoryCustom {
  private static final String ID_FIELD = "_id";
  private static final String PHONE_FIELD = "phone";

  private final ReactiveMongoTemplate mongoTemplate;

  public UserRepositoryImpl(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Mono<User> findAndModify(final UserUpdate userUpdate, final String userId) {
    final Criteria criteria = Criteria.where(ID_FIELD).is(userId);

    final Update update = new Update();
    if (userUpdate.getPhone() != null) {
      update.set(PHONE_FIELD, userUpdate.getPhone());
    }

    return mongoTemplate.findAndModify(
        new Query(criteria), update, new FindAndModifyOptions().returnNew(true), User.class);
  }
}
