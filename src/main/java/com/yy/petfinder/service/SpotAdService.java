package com.yy.petfinder.service;

import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.rest.model.PetSearchRequest;
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

  public SpotAdService(final SpotAdRepository spotAdRepository, final PetAdService petAdService) {
    this.spotAdRepository = spotAdRepository;
    this.petAdService = petAdService;
  }

  public Mono<SpotAdView> createAd(final SpotAdView spotAdView) {
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

    final PetSearchRequest searchRequest = PetSearchRequest
      .builder()
      .latitude(spotAdView.getLatitude())
      .longitude(spotAdView.getLongitude())
      .radius(spotAdView.getRadius())
      .petType(spotAdView.getPetType())
      .build();

    Mono.fromRunnable(
            () -> {
              try {
                Thread.sleep(6000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("ha!");
            })
        .subscribeOn(Schedulers.newElastic("sendEmails"))
        .subscribe();

    return spotAdRepository.save(spotAd).map(ignore -> spotAdView.toBuilder().id(id).build());
  }
}
