package com.yy.petfinder.persistence;

import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.SpotAdRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

public class SpotAdRepositoryImpl implements SpotAdRepositoryCustom {
  private static final String ID_FIELD = "_id";
  private static final String POINT_FIELD = "point";
  private static final String PET_TYPE_FIELD = "petType";

  private final ReactiveMongoTemplate mongoTemplate;

  public SpotAdRepositoryImpl(final ReactiveMongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Flux<SpotAd> findSpotAds(final SpotAdRequest spotAdRequest, final Paging paging) {
    final GeoJsonPoint point =
        new GeoJsonPoint(spotAdRequest.getLongitude(), spotAdRequest.getLatitude());
    final Criteria criteria =
        Criteria.where(POINT_FIELD).nearSphere(point).maxDistance(spotAdRequest.getRadius());
    criteria.and(PET_TYPE_FIELD).is(spotAdRequest.getPetType());

    final Pageable pageable =
        PageRequest.of(0, paging.getPageSize(), Sort.by(Sort.Direction.DESC, ID_FIELD));

    return mongoTemplate.find(new Query(criteria).with(pageable), SpotAd.class);
  }
}
