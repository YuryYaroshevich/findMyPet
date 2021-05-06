package com.yy.petfinder.service;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.PrivateUserView;
import com.yy.petfinder.rest.model.SpotAdView;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SpotAdService {
  private final SpotAdRepository spotAdRepository;
  private final PetAdService petAdService;
  private final UserService userService;
  private final EmailService emailService;

  public SpotAdService(
      final SpotAdRepository spotAdRepository,
      final PetAdService petAdService,
      final UserService userService,
      final EmailService emailService) {
    this.spotAdRepository = spotAdRepository;
    this.petAdService = petAdService;
    this.userService = userService;
    this.emailService = emailService;
  }

  public Mono<SpotAdView> createAd(final SpotAdView spotAdView) {
    final PetSearchRequest searchRequest =
        PetSearchRequest.builder()
            .latitude(spotAdView.getLatitude())
            .longitude(spotAdView.getLongitude())
            .radius(spotAdView.getRadius())
            .petType(spotAdView.getPetType())
            .build();
    petAdService
        .searchAllPets(searchRequest)
        .map(PetAd::getOwnerId)
        .flatMap(ownerId -> userService.getUser(ownerId))
        .map(PrivateUserView::getEmail)
        .flatMap(email -> emailService.sendSpotAdEmail(email, spotAdView.getEmailMessageData()))
        .subscribeOn(Schedulers.parallel())
        .subscribe();

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
