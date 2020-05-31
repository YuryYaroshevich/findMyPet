package com.yy.petfinder.service;

import com.yy.petfinder.exception.OwnerIdUpdateException;
import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAdService {
  private static final boolean IS_FOUND = false;

  private final PetAdRepository petAdRepository;

  public PetAdService(final PetAdRepository petAdRepository) {
    this.petAdRepository = petAdRepository;
  }

  public Mono<PetAdView> createAd(final PetAdView petAdView) {
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();

    final PetAd newPetAd = toPetAd(objectId, uuid, petAdView, IS_FOUND);

    final Mono<PetAd> createdAd = petAdRepository.save(newPetAd);
    return createdAd.map(ad -> petAdView);
  }

  public Mono<PetAdView> getAd(final String uuid) {
    final Mono<PetAd> petAd = petAdRepository.findByUuid(uuid);
    return petAd.map(this::toPetAdView);
  }

  public Mono<List<PetAdView>> searchPets(final PetSearchRequest petSearchReq) {
    return petAdRepository.findPetAds(petSearchReq).map(this::toPetAdView).collectList();
  }

  public Mono<PetAdView> updateAd(String id, final PetAdView updatedAdView) {
    final String uuid = updatedAdView.getId();
    final String ownerId = updatedAdView.getOwnerId();
    final Mono<PetAd> petAd = petAdRepository.findByUuid(id);
    return petAd
        .doOnNext(ad -> ownerIdShouldBeSame(ad.getOwnerId(), ownerId, ad.getUuid()))
        .map(PetAd::getId)
        .map(objectId -> toPetAd(objectId, uuid, updatedAdView, updatedAdView.getFound()))
        .flatMap(petAdRepository::findAndModify)
        .map(this::toPetAdView);
  }

  private void ownerIdShouldBeSame(
      final String ownerIdFromAd, final String ownerIdFromView, final String adUuid) {
    if (!ownerIdFromAd.equals(ownerIdFromView)) {
      throw new OwnerIdUpdateException(ownerIdFromAd, ownerIdFromView, adUuid);
    }
  }

  private PetAd toPetAd(final ObjectId objectId, final String uuid,
                        final PetAdView petAdView, final boolean found) {
    final SearchArea searchArea = SearchArea.of(petAdView.getSearchArea().getCoordinates());
    return PetAd.builder()
        .id(objectId)
        .uuid(uuid)
        .colors(petAdView.getColors())
        .imageBlob(petAdView.getImageBlob())
        .ownerId(petAdView.getOwnerId())
        .name(petAdView.getName())
        .petType(petAdView.getPetType())
        .searchArea(searchArea)
        .found(found)
        .build();
  }

  private PetAdView toPetAdView(final PetAd petAd) {
    return PetAdView.builder()
        .id(petAd.getUuid())
        .colors(petAd.getColors())
        .imageBlob(petAd.getImageBlob())
        .ownerId(petAd.getOwnerId())
        .name(petAd.getName())
        .petType(petAd.getPetType())
        .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
        .found(petAd.isFound())
        .build();
  }
}
