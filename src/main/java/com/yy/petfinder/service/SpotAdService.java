package com.yy.petfinder.service;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SpotAdView;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SpotAdService {
  private final SpotAdRepository spotAdRepository;
  private final PetAdService petAdService;
  private final NotificationService notificationService;

  public SpotAdService(
      final SpotAdRepository spotAdRepository,
      final PetAdService petAdService,
      final NotificationService notificationService) {
    this.spotAdRepository = spotAdRepository;
    this.petAdService = petAdService;
    this.notificationService = notificationService;
  }

  public Mono<SpotAdView> createAd(final SpotAdView spotAdView) {
    final PetSearchRequest searchRequest =
        PetSearchRequest.builder()
            .latitude(spotAdView.getLatitude())
            .longitude(spotAdView.getLongitude())
            .radius(spotAdView.getRadius())
            .petType(spotAdView.getPetType())
            .build();
    final Flux<String> usersToNotify =
        petAdService.searchAllPets(searchRequest).map(PetAd::getOwnerId);
    notificationService.notifyUsers(usersToNotify, spotAdView.getEmailMessageData());

    final String id = new ObjectId().toHexString();
    final SpotAd spotAd =
        SpotAd.builder()
            .id(id)
            .petType(spotAdView.getPetType())
            .description(spotAdView.getDescription())
            .photoIds(spotAdView.getPhotoIds())
            .radius(spotAdView.getRadius())
            .point(List.of(spotAdView.getLongitude(), spotAdView.getLatitude()))
            .build();
    return spotAdRepository.save(spotAd).map(ignore -> spotAdView.toBuilder().id(id).build());
  }
}
