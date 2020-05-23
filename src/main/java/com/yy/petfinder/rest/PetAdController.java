package com.yy.petfinder.rest;

import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.service.PetAdService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pets/ad")
public class PetAdController {
  private final PetAdService petAdService;

  public PetAdController(PetAdService petAdService) {
    this.petAdService = petAdService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PetAdView> createPetAd(@RequestBody PetAdView petAd) {
    return petAdService.createAd(petAd);
  }

  @GetMapping("/{uuid}")
  public Mono<PetAdView> getPetAd(@PathVariable("uuid") final String uuid) {
    return petAdService.getAd(uuid);
  }

  @GetMapping
  public Mono<List<PetAdView>> searchPet(final PetSearchRequest petSearchReq
      /*@RequestParam final double longitude,
      @RequestParam final double latitude,
      @RequestParam final double radius,
      @RequestParam final String petType*/ ) {
    /*final PetSearchRequest petSearchReq =
    PetSearchRequest.builder()
        .longitude(longitude)
        .latitude(latitude)
        .radius(radius)
        .petType(PetType.of(petType))
        .build();*/
    return petAdService.searchPets(petSearchReq);
  }
}
