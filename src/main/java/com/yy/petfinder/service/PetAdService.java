package com.yy.petfinder.service;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAdService {
  private final PetAdRepository petAdRepository;

  public PetAdService(PetAdRepository petAdRepository) {
    this.petAdRepository = petAdRepository;
  }

  public Mono<PetAdView> createAd(PetAdView petAdView) {
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();

    final SearchArea searchArea = SearchArea.of(petAdView.getSearchArea().getCoordinates());
    final PetAd newPetAd =
        PetAd.builder()
            .id(objectId)
            .uuid(uuid)
            .color(petAdView.getColor())
            .imageBlob(petAdView.getImageBlob())
            .ownerId(petAdView.getOwnerId())
            .name(petAdView.getName())
            .petType(petAdView.getPetType())
            .searchArea(searchArea)
            .build();

    final Mono<PetAd> createdAd = petAdRepository.save(newPetAd);
    return createdAd.map(ad -> petAdView);
  }

  public Mono<PetAdView> getAd(String uuid) {
    final Mono<PetAd> petAd = petAdRepository.findByUuid(uuid);
    return petAd.map(this::toView);
  }

  private PetAdView toView(PetAd petAd) {
    return PetAdView.builder()
        .uuid(petAd.getUuid())
        .color(petAd.getColor())
        .imageBlob(petAd.getImageBlob())
        .ownerId(petAd.getOwnerId())
        .name(petAd.getName())
        .petType(petAd.getPetType())
        .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinates()))
        .build();
  }

  public Mono<List<PetAdView>> searchPets() {
    return null;
  }
}
