package com.yy.petfinder.service;

import com.yy.petfinder.exception.InvalidSearchAreaException;
import com.yy.petfinder.exception.PetAdNotFoundException;
import com.yy.petfinder.model.*;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.PetAdResolutionRepository;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdResponse;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PetAdService {
  private final PetAdRepository petAdRepository;
  private final PetAdResolutionRepository petAdResolutionRepository;
  private final Clock clock;

  public PetAdService(
      final PetAdRepository petAdRepository,
      final PetAdResolutionRepository petAdResolutionRepository,
      final Clock clock) {
    this.petAdRepository = petAdRepository;
    this.petAdResolutionRepository = petAdResolutionRepository;
    this.clock = clock;
  }

  public Mono<PetAdResponse> createAd(final PetAdView petAdView, String userId) {
    final String id = new ObjectId().toHexString();

    final PetAd newPetAd = toPetAd(id, petAdView, userId);

    final Mono<PetAd> createdAd =
        petAdRepository.save(newPetAd).onErrorMap(this::mapDataIntegrityError);
    return createdAd.map(this::toPetAdResponse);
  }

  private Throwable mapDataIntegrityError(Throwable exception) {
    if ((exception instanceof DataIntegrityViolationException
            || exception instanceof UncategorizedMongoDbException)
        && exception.getMessage().contains("Can't extract geo keys")) {
      return new InvalidSearchAreaException();
    }
    return exception;
  }

  public Mono<PetAdResponse> getAd(final String id) {
    final Mono<PetAd> petAd = petAdRepository.findById(id);
    return petAd
        .map(this::toPetAdResponse)
        .switchIfEmpty(Mono.error(new PetAdNotFoundException(id)));
  }

  public Mono<List<PetAdResponse>> searchPets(final PetSearchRequest petSearchReq, Paging paging) {
    return petAdRepository
        .findPetAds(petSearchReq, paging)
        .map(this::toPetAdResponse)
        .collectList();
  }

  public Flux<PetAd> searchAllPets(final PetSearchRequest petSearchReq) {
    return petAdRepository.findPetAds(petSearchReq, new Paging(Integer.MAX_VALUE));
  }

  public Mono<PetAdResponse> updateAd(
      final String id, final PetAdView updatedAdView, final String userId) {
    final PetAd updatedPetAd = toPetAd(id, updatedAdView, userId);
    return petAdRepository
        .findAndModify(updatedPetAd, userId)
        .onErrorMap(this::mapDataIntegrityError)
        .map(this::toPetAdResponse)
        .switchIfEmpty(Mono.error(new PetAdNotFoundException(id)));
  }

  public Mono<PetAdResolution> deletePetAd(
      final String id, final PetAdState petAdState, final String userId) {
    return petAdRepository
        .findByIdAndOwnerId(id, userId)
        .switchIfEmpty(Mono.error(new PetAdNotFoundException(id)))
        .flatMap(
            petAd ->
                petAdRepository
                    .deleteById(id)
                    .thenReturn(petAd)
                    .map(
                        ignore ->
                            PetAdResolution.builder()
                                .id(id)
                                .petAdState(petAdState)
                                .petType(petAd.getPetType())
                                .breed(petAd.getBreed())
                                .searchArea(petAd.getSearchArea())
                                .createdAt(Instant.now(clock))
                                .build()))
        .flatMap(petAdResolution -> petAdResolutionRepository.save(petAdResolution));
  }

  public Mono<List<PetAdResponse>> getAds(String userId) {
    return petAdRepository.findPetAdsByOwnerId(userId).map(this::toPetAdResponse).collectList();
  }

  private PetAd toPetAd(final String id, final PetAdView petAdView, String userId) {
    final SearchArea searchArea = SearchArea.of(petAdView.getSearchArea().getCoordinates());
    return PetAd.builder()
        .id(id)
        .colors(petAdView.getColors())
        .photoUrls(petAdView.getPhotoUrls())
        .ownerId(userId)
        .name(petAdView.getName())
        .petType(petAdView.getPetType())
        .breed(petAdView.getBreed())
        .searchArea(searchArea)
        .createdAt(petAdView.getCreatedAt() == null ? Instant.now(clock) : petAdView.getCreatedAt())
        .build();
  }

  private PetAdResponse toPetAdResponse(final PetAd petAd) {
    return PetAdResponse.builder()
        .id(petAd.getId())
        .colors(petAd.getColors())
        .photoUrls(petAd.getPhotoUrls())
        .name(petAd.getName())
        .petType(petAd.getPetType())
        .breed(petAd.getBreed())
        .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
        .ownerId(petAd.getOwnerId())
        .createdAt(petAd.getCreatedAt())
        .build();
  }
}
