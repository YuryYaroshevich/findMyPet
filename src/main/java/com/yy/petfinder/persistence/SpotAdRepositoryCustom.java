package com.yy.petfinder.persistence;

import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.SpotAdRequest;
import reactor.core.publisher.Flux;

public interface SpotAdRepositoryCustom {
  Flux<SpotAd> findSpotAds(SpotAdRequest spotAdRequest, Paging paging);
}
