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
  private static final String MESSENGERS_FIELD = "messengers";
  private static final String PASSWORD_FIELD = "password";

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
    if (userUpdate.getMessengers() != null) {
      update.set(MESSENGERS_FIELD, userUpdate.getMessengers());
    }
    if (userUpdate.getPasswordUpdate() != null) {
      update.set(PASSWORD_FIELD, userUpdate.getPasswordUpdate().getNewPassword());
    }

    return mongoTemplate.findAndModify(
        new Query(criteria), update, new FindAndModifyOptions().returnNew(true), User.class);
  }
}
