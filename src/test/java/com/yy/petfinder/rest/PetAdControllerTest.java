package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.SearchAreaView;
import com.yy.petfinder.security.service.TokenService;
import com.yy.petfinder.util.WebTestClientWrapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private TokenService tokenService;

  private String authHeaderValue;
  private String userId;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
    userRepository.deleteAll().block();
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    userId = user.getId();
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
  public void testGetPetAdReturnsNotFoundIfNoPetAdWithGivenId() {
    // given
    final String petAdId = "notExistsId";

    // when
    final Map<String, String> errorResp =
        webTestClient
            .get()
            .uri("/pets/ad/" + petAdId)
            .header(AUTHORIZATION, authHeaderValue)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "PetAd with provided id not found: id=" + petAdId;
    assertEquals(errorMsg, errorResp.get("message"));
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
    final List<String> photoUrls =
        List.of("https://host.com/image1", "https://host.com/image2", "https://host.com/image3");
    final List<String> colors = List.of("black", "brown");
    final PetAdView petAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(coordinates))
            .petType(petType)
            .name(name)
            .photoUrls(photoUrls)
            .colors(colors)
            .build();

    // when
    webTestClient
        .post()
        .uri("/pets/ad")
        .header(AUTHORIZATION, authHeaderValue)
        .bodyValue(petAdView)
        .exchange()
        .expectStatus()
        .isCreated();

    // then
    assertEquals(Long.valueOf(1), petAdRepository.count().block());
    final List<PetAd> petAds = petAdRepository.findAll().collectList().block();
    assertEquals(1, petAds.size());

    final PetAd petAd = petAds.get(0);
    assertEquals(
        petAdView.getSearchArea().getCoordinates(), petAd.getSearchArea().getCoordinatesList());
    assertEquals(petAdView.getPetType(), petAd.getPetType());
    assertEquals(petAdView.getName(), petAd.getName());
    assertEquals(petAdView.getPhotoUrls(), petAd.getPhotoUrls());
    assertEquals(petAdView.getColors(), petAd.getColors());
  }

  @Test
  public void testCreatePetAdWithoutAuthHeaderFails() {
    // given
    final List<List<Double>> coordinates =
        List.of(
            List.of(53.911665, 27.469369),
            List.of(53.911867, 27.491685),
            List.of(53.899226, 27.491856),
            List.of(53.897405, 27.461129),
            List.of(53.911665, 27.469369));
    final PetAdView petAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(coordinates))
            .petType(PetType.DOG)
            .name("Fido")
            .photoUrls(List.of("https://host.com/image1"))
            .colors(List.of("black", "brown"))
            .build();

    // when then
    webTestClient
        .post()
        .uri("/pets/ad")
        .bodyValue(petAdView)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  public void testCreatePetAdWithInvalidTokenUnauthorized() {
    // given
    final List<List<Double>> coordinates =
        List.of(
            List.of(53.911665, 27.469369),
            List.of(53.911867, 27.491685),
            List.of(53.899226, 27.491856),
            List.of(53.897405, 27.461129),
            List.of(53.911665, 27.469369));
    final PetAdView petAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(coordinates))
            .petType(PetType.DOG)
            .name("Fido")
            .photoUrls(List.of("https://host.com/image1"))
            .colors(List.of("black", "brown"))
            .build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/pets/ad")
            .header(AUTHORIZATION, "Bearer invalidToken")
            .bodyValue(petAdView)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    assertEquals("Invalid token provided", errorResp.get("message"));
  }

  @Test
  public void testUpdatePetAdUpdatesFields() {
    // given
    final PetAd petAd = petAdBuilderWithDefaults().ownerId(userId).build();
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
            "https://res.cloudinary.com/demo/image3",
            "https://res.cloudinary.com/demo/image4");
    final List<String> newColors = List.of("black", "brown", "white");
    final PetAdView updatedPetAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(newCoordinates))
            .petType(newPetType)
            .name(newName)
            .photoUrls(photoUrls)
            .colors(newColors)
            .id(petAd.getId())
            .build();

    // when
    webTestClient
        .put()
        .uri("/pets/ad/" + petAd.getId())
        .header(AUTHORIZATION, authHeaderValue)
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
  public void testUpdatePetAdReturnsNotFoundIfNoPetAdWithGivenId() {
    // given
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
            "https://res.cloudinary.com/demo/image3",
            "https://res.cloudinary.com/demo/image4");
    final List<String> newColors = List.of("black", "brown", "white");
    final String petAdId = "notExistId";
    final PetAdView updatedPetAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(newCoordinates))
            .petType(newPetType)
            .name(newName)
            .photoUrls(photoUrls)
            .colors(newColors)
            .id(petAdId)
            .build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .put()
            .uri("/pets/ad/" + petAdId)
            .header(AUTHORIZATION, authHeaderValue)
            .bodyValue(updatedPetAdView)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "PetAd with provided id not found: id=" + petAdId;
    assertEquals(errorMsg, errorResp.get("message"));
  }

  @Test
  public void testUpdatePetAdWithoutAuthHeaderFails() {
    // given
    final PetAd petAd = petAdBuilderWithDefaults().ownerId(userId).build();
    petAdRepository.save(petAd).block();

    final List<List<Double>> newCoordinates =
        List.of(
            List.of(53.911665, 27.469369),
            List.of(53.911867, 27.491685),
            List.of(53.899226, 27.491856),
            List.of(53.897405, 27.461129),
            List.of(53.911665, 27.469369));
    final PetAdView updatedPetAdView =
        PetAdView.builder()
            .searchArea(new SearchAreaView(newCoordinates))
            .petType(PetType.DOG)
            .name("Fido")
            .photoUrls(List.of("https://res.cloudinary.com/demo/image1"))
            .colors(List.of("black", "brown", "white"))
            .id(petAd.getId())
            .build();

    // when
    webTestClient
        .put()
        .uri("/pets/ad/" + petAd.getId())
        .bodyValue(updatedPetAdView)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
