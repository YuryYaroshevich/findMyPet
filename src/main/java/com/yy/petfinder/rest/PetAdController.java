package com.yy.petfinder.rest;

import static com.yy.petfinder.util.UserIdRetriever.userIdFromContext;

import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.service.PetAdService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pets/ad")
public class PetAdController {
  public static final String NEXT_PAGE_TOKEN = "Next-page-token";

  private final PetAdService petAdService;

  public PetAdController(PetAdService petAdService) {
    this.petAdService = petAdService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PetAdView> createPetAd(@RequestBody PetAdView petAd) {
    return userIdFromContext().flatMap(userId -> petAdService.createAd(petAd, userId));
  }

  @GetMapping("/{id}")
  public Mono<PetAdView> getPetAd(@PathVariable("id") final String id) {
    return petAdService.getAd(id);
  }

  @PutMapping("/{id}")
  public Mono<PetAdView> updatePetAd(@PathVariable String id, @RequestBody PetAdView petAdView) {
    return userIdFromContext().flatMap(userId -> petAdService.updateAd(id, petAdView, userId));
  }

  @GetMapping
  public Mono<ResponseEntity<List<PetAdView>>> searchPet(
      final PetSearchRequest petSearchReq, final Paging paging) {
    return petAdService
        .searchPets(petSearchReq, paging)
        .map(petAds -> createResponse(petAds, paging.getPageSize()));
  }

  private ResponseEntity<List<PetAdView>> createResponse(
      final List<PetAdView> petAds, final int pageSize) {
    final ResponseEntity.BodyBuilder respBuilder = ResponseEntity.ok();
    if (pageSize == petAds.size()) {
      respBuilder.header(NEXT_PAGE_TOKEN, getNextPageToken(petAds));
    }
    return respBuilder.body(petAds);
  }

  private static String getNextPageToken(final List<PetAdView> petAds) {
    final int lastPetAdIndex = petAds.size() - 1;
    return petAds.get(lastPetAdIndex).getId();
  }
}
