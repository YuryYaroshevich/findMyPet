package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.SearchAreaView;
import com.yy.petfinder.util.WebTestClientWrapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
  }

  @Test
  public void testGetPetAdReturnsCorrectAd() {
    // given
    final PetAd petAd = petAdBuilderWithDefaults().build();
    final PetAdView expectedPetAd =
        PetAdView.builder()
            .id(petAd.getId())
            .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
            .petType(petAd.getPetType())
            .name(petAd.getName())
            .ownerId(petAd.getOwnerId())
            .photoUrls(petAd.getPhotoUrls())
            .colors(petAd.getColors())
            .build();

    petAdRepository.save(petAd).block();

    // when
    final PetAdView petAdView =
        WebTestClientWrapper.get(webTestClient, "/pets/ad/" + petAd.getId(), PetAdView.class);

    // then
    assertEquals(expectedPetAd, petAdView);
  }

  @Test
  public void testCreatePetAdSavesAdInDb() {
    // given
    final List<List<Double>> coordinates =
        List.of(
            List.of(53.911665, 27.469369),
            List.of(53.911867, 27.491685),
            List.of(53.899226, 27.491856),
            List.of(53.897405, 27.461129),
            List.of(53.911665, 27.469369));
    final PetType petType = PetType.DOG;
    final String name = "Fido";
    final String ownerId = UUID.randomUUID().toString();
    final List<String> photoUrls =
        List.of("https://host.com/image1", "https://host.com/image2", "https://host.com/image3");
    final List<String> colors = List.of("black", "brown");
    final PetAdView petAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(coordinates))
            .petType(petType)
            .name(name)
            .ownerId(ownerId)
            .photoUrls(photoUrls)
            .colors(colors)
            .build();

    // when
    webTestClient.post().uri("/pets/ad").bodyValue(petAdView).exchange().expectStatus().isCreated();

    // then
    assertEquals(Long.valueOf(1), petAdRepository.count().block());
    final List<PetAd> petAds = petAdRepository.findAll().collectList().block();
    assertEquals(1, petAds.size());

    final PetAd petAd = petAds.get(0);
    assertEquals(
        petAdView.getSearchArea().getCoordinates(), petAd.getSearchArea().getCoordinatesList());
    assertEquals(petAdView.getPetType(), petAd.getPetType());
    assertEquals(petAdView.getName(), petAd.getName());
    assertEquals(petAdView.getOwnerId(), petAd.getOwnerId());
    assertEquals(petAdView.getPhotoUrls(), petAd.getPhotoUrls());
    assertEquals(petAdView.getColors(), petAd.getColors());
  }

  @Test
  public void testUpdatePetAdUpdatesFields() {
    // given
    final PetAd petAd = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd).block();

    final List<List<Double>> newCoordinates =
        List.of(
            List.of(53.911665, 27.469369),
            List.of(53.911867, 27.491685),
            List.of(53.899226, 27.491856),
            List.of(53.897405, 27.461129),
            List.of(53.911665, 27.469369));
    final PetType newPetType = PetType.DOG;
    final String newName = "Fido";
    final List<String> photoUrls =
        List.of(
            "https://res.cloudinary.com/demo/image1",
            "https://res.cloudinary.com/demo/image2",
            "https://res.cloudinary.com/demo/image3");
    final List<String> newColors = List.of("black", "brown", "white");
    final PetAdView updatedPetAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(newCoordinates))
            .petType(newPetType)
            .name(newName)
            .ownerId(petAd.getOwnerId())
            .photoUrls(photoUrls)
            .colors(newColors)
            .id(petAd.getId())
            .build();

    // when
    webTestClient
        .put()
        .uri("/pets/ad/" + petAd.getId())
        .bodyValue(updatedPetAdView)
        .exchange()
        .expectStatus()
        .isOk();

    // then
    final PetAd updatedPetAd = petAdRepository.findById(petAd.getId()).block();
    assertEquals(
        updatedPetAdView.getSearchArea().getCoordinates(),
        updatedPetAd.getSearchArea().getCoordinatesList());
    assertEquals(updatedPetAdView.getPetType(), updatedPetAd.getPetType());
    assertEquals(updatedPetAdView.getName(), updatedPetAd.getName());
    assertEquals(petAd.getOwnerId(), updatedPetAd.getOwnerId());
    assertEquals(updatedPetAdView.getPhotoUrls(), updatedPetAd.getPhotoUrls());
    assertEquals(updatedPetAdView.getColors(), updatedPetAd.getColors());
    assertEquals(updatedPetAdView.isFound(), updatedPetAd.isFound());
  }

  @Test
  public void testMarkAsFoundUpdatesPetAdAsResolved() {}
}
