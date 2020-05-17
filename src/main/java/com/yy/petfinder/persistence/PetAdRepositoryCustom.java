package com.yy.petfinder.persistence;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.rest.model.PetSearchRequest;
import reactor.core.publisher.Flux;

public interface PetAdRepositoryCustom {
  Flux<PetAd> findPetAds(PetSearchRequest petSearchRequest);
}
