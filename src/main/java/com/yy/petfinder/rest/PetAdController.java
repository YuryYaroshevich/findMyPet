package com.yy.petfinder.rest;

import static com.yy.petfinder.util.UserIdRetriever.userIdFromContext;

import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdResponse;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.service.PetAdService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PetAdController {
  public static final String NEXT_PAGE_TOKEN = "Next-page-token";
  public static final int DEFAULT_PAGE_SIZE = 10;

  private final PetAdService petAdService;

  public PetAdController(PetAdService petAdService) {
    this.petAdService = petAdService;
  }

  @PostMapping("/pets/ad")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PetAdResponse> createPetAd(@RequestBody @Valid final PetAdView petAd) {
    return userIdFromContext().flatMap(userId -> petAdService.createAd(petAd, userId));
  }

  @GetMapping("/pets/ad/{id}")
  public Mono<PetAdResponse> getPetAd(@PathVariable("id") final String id) {
    return petAdService.getAd(id);
  }

  @PutMapping("/pets/ad/{id}")
  public Mono<PetAdResponse> updatePetAd(
      @PathVariable final String id, @RequestBody @Valid final PetAdView petAdView) {
    return userIdFromContext().flatMap(userId -> petAdService.updateAd(id, petAdView, userId));
  }

  @GetMapping("/pets/ad")
  public Mono<ResponseEntity<List<PetAdResponse>>> searchPet(
      final PetSearchRequest petSearchReq, final Paging paging) {
    return petAdService
        .searchPets(petSearchReq, paging)
        .map(petAds -> createResponse(petAds, paging.getNextPageToken()));
  }

  private ResponseEntity<List<PetAdResponse>> createResponse(
      final List<PetAdResponse> petAds, final String oldNextPageToken) {
    final ResponseEntity.BodyBuilder respBuilder = ResponseEntity.ok();
    respBuilder.header(NEXT_PAGE_TOKEN, getNextPageToken(petAds, oldNextPageToken));
    return respBuilder.body(petAds);
  }

  private static String getNextPageToken(
      final List<PetAdResponse> petAds, final String oldNextPageToken) {
    if (petAds.isEmpty()) {
      return oldNextPageToken;
    }
    final int lastPetAdIndex = petAds.size() - 1;
    return petAds.get(lastPetAdIndex).getId();
  }

  @GetMapping("/pets/user/ad")
  public Mono<List<PetAdResponse>> getAccountPetAds() {
    return userIdFromContext().flatMap(userId -> petAdService.getAds(userId));
  }
}
