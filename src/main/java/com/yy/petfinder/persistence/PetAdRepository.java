package com.yy.petfinder.persistence;

import com.yy.petfinder.model.PetAd;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PetAdRepository
    extends ReactiveCrudRepository<PetAd, String>, PetAdRepositoryCustom {
  Mono<PetAd> findByUuid(String uuid);
}
