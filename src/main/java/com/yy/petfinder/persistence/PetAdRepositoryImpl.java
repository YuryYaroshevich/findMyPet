package com.yy.petfinder.persistence;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetSearchRequest;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PetAdRepositoryImpl implements PetAdRepositoryCustom {
  private static final String SEARCH_AREA_FIELD = "searchArea";
  private static final String PET_TYPE_FIELD = "petType";
  private static final String BREED_FIELD = "breed";
  private static final String COLORS_FIELD = "colors";
  private static final String ID_FIELD = "_id";
  private static final String OWNER_ID_FIELD = "ownerId";
  private static final String FOUND_FIELD = "found";
  private static final String NAME_FILED = "name";
  private static final String IMAGE_FILED = "imageBlob";

  private final ReactiveMongoTemplate mongoTemplate;

  public PetAdRepositoryImpl(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Flux<PetAd> findPetAds(final PetSearchRequest petSearchReq, final Paging paging) {
    final GeoJsonPoint point =
        new GeoJsonPoint(petSearchReq.getLongitude(), petSearchReq.getLatitude());
    final Criteria criteria =
        Criteria.where(SEARCH_AREA_FIELD).nearSphere(point).maxDistance(petSearchReq.getRadius());
    if (petSearchReq.getPetType() != null) {
      criteria.and(PET_TYPE_FIELD).is(petSearchReq.getPetType());
    }
    if (petSearchReq.getBreed() != null) {
      criteria.and(BREED_FIELD).is(petSearchReq.getBreed());
    }
    if (petSearchReq.getColors() != null) {
      criteria.and(COLORS_FIELD).in(petSearchReq.getColors());
    }
    criteria.and(FOUND_FIELD).is(false);

    if (paging.getNextPageToken() != null) {
      criteria.and(ID_FIELD).lt(new ObjectId(paging.getNextPageToken()));
    }

    final Pageable pageable =
        PageRequest.of(0, paging.getPageSize(), Sort.by(Sort.Direction.DESC, ID_FIELD));

    return mongoTemplate.find(new Query(criteria).with(pageable), PetAd.class);
  }

  @Override
  public Mono<PetAd> findAndModify(final PetAd updatedPetAd, String userId) {
    final Criteria criteria =
        Criteria.where(ID_FIELD).is(updatedPetAd.getId()).and(OWNER_ID_FIELD).is(userId);

    final Update update = new Update();
    update.set(PET_TYPE_FIELD, updatedPetAd.getPetType());
    if (updatedPetAd.getColors() != null) {
      update.set(COLORS_FIELD, updatedPetAd.getColors());
    }
    if (updatedPetAd.getBreed() != null) {
      update.set(COLORS_FIELD, updatedPetAd.getBreed());
    }
    update.set(NAME_FILED, updatedPetAd.getName());
    update.set(IMAGE_FILED, updatedPetAd.getPhotoUrls());
    update.set(SEARCH_AREA_FIELD, updatedPetAd.getSearchArea());
    update.set(FOUND_FIELD, updatedPetAd.isFound());

    return mongoTemplate.findAndModify(
        new Query(criteria), update, new FindAndModifyOptions().returnNew(true), PetAd.class);
  }
}
