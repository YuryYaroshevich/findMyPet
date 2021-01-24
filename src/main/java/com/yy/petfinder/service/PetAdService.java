package com.yy.petfinder.service;

import com.yy.petfinder.exception.InvalidSearchAreaException;
import com.yy.petfinder.exception.PetAdNotFoundException;
import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdResponse;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAdService {
  private final PetAdRepository petAdRepository;

  public PetAdService(final PetAdRepository petAdRepository) {
    this.petAdRepository = petAdRepository;
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

  public Mono<PetAdResponse> updateAd(
      final String id, final PetAdView updatedAdView, final String userId) {
    final PetAd updatedPetAd = toPetAd(id, updatedAdView, userId);
    return petAdRepository
        .findAndModify(updatedPetAd, userId)
        .onErrorMap(this::mapDataIntegrityError)
        .map(this::toPetAdResponse)
        .switchIfEmpty(Mono.error(new PetAdNotFoundException(id)));
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
        .petAdStatus(petAdView.getPetAdStatus())
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
        .petAdStatus(petAd.getPetAdStatus())
        .ownerId(petAd.getOwnerId())
        .build();
  }
}
