package com.yy.petfinder.rest;

import static com.yy.petfinder.util.PaginatedResponseHelper.createResponse;

import com.yy.petfinder.rest.model.*;
import com.yy.petfinder.service.SpotAdService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pets/spotAd")
public class SpotAdController {
  private final SpotAdService spotAdService;

  public SpotAdController(final SpotAdService spotAdService) {
    this.spotAdService = spotAdService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<SpotAdView> createSpotAd(@RequestBody @Valid final SpotAdView spotAdView) {
    return spotAdService.createAd(spotAdView);
  }

  @GetMapping
  public Mono<ResponseEntity<List<SpotAdResponse>>> getSpotAds(
      final SpotAdRequest spotAdRequest, final Paging paging) {
    return spotAdService.getAds(spotAdRequest, paging).map(ads -> createResponse(ads, paging));
  }

  @GetMapping("{id}")
  public Mono<SpotAdResponse> getSpotAd(@PathVariable final String id) {
    return spotAdService.getAd(id);
  }
}
