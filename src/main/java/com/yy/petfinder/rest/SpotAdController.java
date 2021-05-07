package com.yy.petfinder.rest;

import com.yy.petfinder.rest.model.SpotAdResponse;
import com.yy.petfinder.rest.model.SpotAdView;
import com.yy.petfinder.service.SpotAdService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
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

  @GetMapping("{id}")
  public Mono<SpotAdResponse> getSpotAd(@PathVariable final String id) {
    return spotAdService.getAd(id);
  }
}
