package com.yy.petfinder.service;

import com.yy.petfinder.exception.SpotAdNotFoundException;
import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.rest.model.*;
import java.time.Clock;
import java.time.Instant;
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
  private final Clock clock;

  public SpotAdService(
      final SpotAdRepository spotAdRepository,
      final PetAdService petAdService,
      final NotificationService notificationService,
      final Clock clock) {
    this.spotAdRepository = spotAdRepository;
    this.petAdService = petAdService;
    this.notificationService = notificationService;
    this.clock = clock;
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

    final String spotAdId = new ObjectId().toHexString();
    notificationService.notifyUsers(usersToNotify, spotAdId, spotAdView.getEmailMessageData());

    final SpotAd spotAd =
        SpotAd.builder()
            .id(spotAdId)
            .petType(spotAdView.getPetType())
            .description(spotAdView.getDescription())
            .phone(spotAdView.getPhone())
            .photoIds(spotAdView.getPhotoIds())
            .radius(spotAdView.getRadius())
            .point(List.of(spotAdView.getLongitude(), spotAdView.getLatitude()))
            .createdAt(Instant.now(clock))
            .build();
    return spotAdRepository.save(spotAd).map(ignore -> spotAdView.toBuilder().id(spotAdId).build());
  }

  public Mono<SpotAdResponse> getAd(final String id) {
    return spotAdRepository
        .findById(id)
        .map(this::toSpotAdResponse)
        .switchIfEmpty(Mono.error(new SpotAdNotFoundException(id)));
  }

  public Mono<List<SpotAdResponse>> getAds(final SpotAdRequest spotAdRequest, final Paging paging) {
    return spotAdRepository
        .findSpotAds(spotAdRequest, paging)
        .map(this::toSpotAdResponse)
        .collectList();
  }

  private SpotAdResponse toSpotAdResponse(final SpotAd spotAd) {
    return SpotAdResponse.builder()
        .id(spotAd.getId())
        .petType(spotAd.getPetType())
        .description(spotAd.getDescription())
        .phone(spotAd.getPhone())
        .photoIds(spotAd.getPhotoIds())
        .longitude(spotAd.getPoint().get(0))
        .latitude(spotAd.getPoint().get(1))
        .radius(spotAd.getRadius())
        .createdAt(spotAd.getCreatedAt())
        .build();
  }
}
