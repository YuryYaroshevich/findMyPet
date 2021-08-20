package com.yy.petfinder.rest;

import static com.yy.petfinder.util.PaginatedResponseHelper.createResponse;
import static com.yy.petfinder.util.UserIdRetriever.userIdFromContext;

import com.yy.petfinder.model.PetAdResult;
import com.yy.petfinder.rest.model.*;
import com.yy.petfinder.service.PetAdService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class PetAdController {

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

  @DeleteMapping("/pets/ad/{id}")
  public Mono<ResponseEntity> deletePetAd(
      @PathVariable final String id, @RequestParam PetAdResult result) {
    return userIdFromContext()
        .flatMap(userId -> petAdService.deletePetAd(id, result, userId))
        .map(ResponseEntity::ok);
  }

  @GetMapping("/pets/ad")
  public Mono<ResponseEntity<List<PetAdResponse>>> searchPet(
      final PetSearchRequest petSearchReq, final Paging paging) {
    return petAdService
        .searchPets(petSearchReq, paging)
        .map(petAds -> createResponse(petAds, paging));
  }

  @GetMapping("/pets/user/ad")
  public Mono<List<PetAdResponse>> getAccountPetAds() {
    return userIdFromContext().flatMap(userId -> petAdService.getAds(userId));
  }
}
