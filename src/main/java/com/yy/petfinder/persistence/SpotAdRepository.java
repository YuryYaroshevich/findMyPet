package com.yy.petfinder.persistence;

import com.yy.petfinder.model.SpotAd;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotAdRepository extends ReactiveCrudRepository<SpotAd, String> {}
