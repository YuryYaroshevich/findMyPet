package com.yy.petfinder.persistence;

import com.yy.petfinder.model.PetAdResolution;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetAdResolutionRepository
    extends ReactiveCrudRepository<PetAdResolution, String> {}
