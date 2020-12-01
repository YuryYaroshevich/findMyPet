package com.yy.petfinder.service;

import com.yy.petfinder.exception.PetAdNotFoundException;
import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAdService {
  private final PetAdRepository petAdRepository;
  private final ImageService imageService;

  public PetAdService(final PetAdRepository petAdRepository, final ImageService imageService) {
    this.petAdRepository = petAdRepository;
    this.imageService = imageService;
  }

  public Mono<PetAdView> createAd(final PetAdView petAdView, String userId) {
    final String id = new ObjectId().toHexString();

    final PetAd newPetAd = toPetAd(id, petAdView, userId);

    final Mono<PetAd> createdAd = petAdRepository.save(newPetAd);
    return createdAd.map(this::toPetAdView);
  }

  public Mono<PetAdView> getAd(final String id) {
    final Mono<PetAd> petAd = petAdRepository.findById(id);
    return petAd.map(this::toPetAdView).switchIfEmpty(Mono.error(new PetAdNotFoundException(id)));
  }

  public Mono<List<PetAdView>> searchPets(final PetSearchRequest petSearchReq, Paging paging) {
    return petAdRepository.findPetAds(petSearchReq, paging).map(this::toPetAdView).collectList();
  }

  public Mono<PetAdView> updateAd(
      final String id, final PetAdView updatedAdView, final String userId) {
    return imageService
        .deleteImages(updatedAdView.getRemovedPhotoUrls())
        .flatMap(
            aVoid -> {
              final PetAd updatedPetAd = toPetAd(id, updatedAdView, userId);
              return petAdRepository
                  .findAndModify(updatedPetAd, userId)
                  .map(this::toPetAdView)
                  .switchIfEmpty(Mono.error(new PetAdNotFoundException(id)));
            });
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
        .found(petAdView.isFound())
        .build();
  }

  private PetAdView toPetAdView(final PetAd petAd) {
    return PetAdView.builder()
        .id(petAd.getId())
        .colors(petAd.getColors())
        .photoUrls(petAd.getPhotoUrls())
        .name(petAd.getName())
        .petType(petAd.getPetType())
        .breed(petAd.getBreed())
        .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
        .found(petAd.isFound())
        .build();
  }
}
