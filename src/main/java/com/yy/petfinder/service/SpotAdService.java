package com.yy.petfinder.service;

import com.yy.petfinder.model.SpotAd;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.rest.model.SpotAdView;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    return spotAdRepository.save(spotAd).map(ad -> spotAdView.toBuilder().id(ad.getId()).build());
  }
}
