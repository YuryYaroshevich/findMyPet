package com.yy.petfinder.service;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
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

  public PetAdService(final PetAdRepository petAdRepository) {
    this.petAdRepository = petAdRepository;
  }

  public Mono<PetAdView> createAd(final PetAdView petAdView) {
    final String id = new ObjectId().toHexString();

    final PetAd newPetAd = toPetAd(id, petAdView);

    final Mono<PetAd> createdAd = petAdRepository.save(newPetAd);
    return createdAd.map(ad -> petAdView);
  }

  public Mono<PetAdView> getAd(final String id) {
    final Mono<PetAd> petAd = petAdRepository.findById(id);
    return petAd.map(this::toPetAdView);
  }

  public Mono<List<PetAdView>> searchPets(final PetSearchRequest petSearchReq) {
    return petAdRepository.findPetAds(petSearchReq).map(this::toPetAdView).collectList();
  }

  public Mono<PetAdView> updateAd(String id, final PetAdView updatedAdView) {
    final PetAd updatedPetAd = toPetAd(id, updatedAdView);
    return petAdRepository.findAndModify(updatedPetAd).map(this::toPetAdView);
  }

  private PetAd toPetAd(final String id, final PetAdView petAdView) {
    final SearchArea searchArea = SearchArea.of(petAdView.getSearchArea().getCoordinates());
    return PetAd.builder()
        .id(id)
        .colors(petAdView.getColors())
        .photoUrls(petAdView.getPhotoUrls())
        .ownerId(petAdView.getOwnerId())
        .name(petAdView.getName())
        .petType(petAdView.getPetType())
        .searchArea(searchArea)
        .found(petAdView.isFound())
        .build();
  }

  private PetAdView toPetAdView(final PetAd petAd) {
    return PetAdView.builder()
        .id(petAd.getId())
        .colors(petAd.getColors())
        .photoUrls(petAd.getPhotoUrls())
        .ownerId(petAd.getOwnerId())
        .name(petAd.getName())
        .petType(petAd.getPetType())
        .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
        .found(petAd.isFound())
        .build();
  }
}
