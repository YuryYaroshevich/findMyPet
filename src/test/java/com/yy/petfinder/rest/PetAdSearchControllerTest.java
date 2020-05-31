package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.util.SearchUriBuilder.searchUri;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdSearchControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
  }

  @ParameterizedTest
  @MethodSource("petAdsAndExpectedSearchResultAndSearchReq")
  public void testSearchPetByNotGeoTraitsWorksCorrectly2(
      final List<PetAd> petAds,
      final Set<String> searchResultPetAdUuids,
      final PetSearchRequest petSearchReq) {

    // given
    petAdRepository.saveAll(petAds).blockLast();

    // when
    final List<PetAdView> petAdViews =
        webTestClient
            .get()
            .uri(searchUri(petSearchReq))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PetAdView.class)
            .returnResult()
            .getResponseBody();
    final Set<String> petAdUuids = petAdViews.stream().map(PetAdView::getId).collect(toSet());

    // then
    assertEquals(searchResultPetAdUuids, petAdUuids);
  }

  private static Stream<Arguments> petAdsAndExpectedSearchResultAndSearchReq() {
    // scenario 1
    final PetAd dogAd = petAdBuilderWithDefaults().petType(PetType.DOG).build();
    final PetAd catAd = petAdBuilderWithDefaults().petType(PetType.CAT).build();

    final PetSearchRequest petSearchRequest1 =
        PetSearchRequest.builder()
            .longitude(27.417068481445312)
            .latitude(53.885826945065915)
            .radius(400)
            .petType(PetType.DOG)
            .build();

    final Arguments scenario1 =
        Arguments.of(List.of(dogAd, catAd), Set.of(dogAd.getUuid()), petSearchRequest1);

    // scenario 2
    final List<String> blackColor = List.of("black");
    final PetAd blackDogAd = petAdBuilderWithDefaults().colors(blackColor).build();
    final PetAd brownDogAd = petAdBuilderWithDefaults().colors(List.of("brown")).build();

    final PetSearchRequest petSearchRequest2 =
        PetSearchRequest.builder()
            .longitude(27.417068481445312)
            .latitude(53.885826945065915)
            .radius(400)
            .colors(blackColor)
            .build();

    final Arguments scenario2 =
        Arguments.of(
            List.of(blackDogAd, brownDogAd), Set.of(blackDogAd.getUuid()), petSearchRequest2);

    // scenario 3
    final String labradorBreed = "labrador";
    final PetAd labradorAd = petAdBuilderWithDefaults().breed(labradorBreed).build();
    final PetAd spanielAd = petAdBuilderWithDefaults().breed("spaniel").build();

    final PetSearchRequest petSearchRequest3 =
        PetSearchRequest.builder()
            .longitude(27.417068481445312)
            .latitude(53.885826945065915)
            .radius(400)
            .breed(labradorBreed)
            .build();

    final Arguments scenario3 =
        Arguments.of(
            List.of(labradorAd, spanielAd), Set.of(labradorAd.getUuid()), petSearchRequest3);

    // scenario 4
    final PetAd brownCatAd =
        petAdBuilderWithDefaults().colors(List.of("brown")).petType(PetType.CAT).build();
    final PetAd brownSpanielAd =
        petAdBuilderWithDefaults()
            .petType(PetType.DOG)
            .colors(List.of("brown"))
            .breed("spaniel")
            .build();
    final PetAd blackAndWhiteLabradorAd =
        petAdBuilderWithDefaults()
            .petType(PetType.DOG)
            .colors(List.of("black", "white"))
            .breed(labradorBreed)
            .build();

    final PetSearchRequest petSearchRequest4 =
        PetSearchRequest.builder()
            .longitude(27.417068481445312)
            .latitude(53.885826945065915)
            .radius(400)
            .breed(labradorBreed)
            .colors(List.of("black", "white"))
            .petType(PetType.DOG)
            .build();

    final Arguments scenario4 =
        Arguments.of(
            List.of(brownCatAd, brownSpanielAd, blackAndWhiteLabradorAd),
            Set.of(blackAndWhiteLabradorAd.getUuid()),
            petSearchRequest4);

    return Stream.of(scenario1, scenario2, scenario3, scenario4);
  }
}
