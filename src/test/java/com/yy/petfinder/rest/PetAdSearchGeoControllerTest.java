package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
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
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdSearchGeoControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
  }

  @ParameterizedTest
  @MethodSource("petAdsAndExpectedSearchResultAndUserCoords")
  public void testSearchPetReturnsAllAdsNearProvidedCoords3(
      final List<PetAd> petAds,
      final Set<String> searchResultPetAdUuids,
      final PetSearchRequest petSearchRequest) {

    // given
    petAdRepository.saveAll(petAds).blockLast();

    // when
    final List<PetAdView> petAdViews =
        webTestClient
            .get()
            .uri(
                searchUri(
                    petSearchRequest.getLongitude(),
                    petSearchRequest.getLatitude(),
                    petSearchRequest.getRadius()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PetAdView.class)
            .returnResult()
            .getResponseBody();
    final Set<String> petAdUuids = petAdViews.stream().map(PetAdView::getUuid).collect(toSet());

    // then
    assertEquals(searchResultPetAdUuids, petAdUuids);
  }

  private String searchUri(final double longitude, final double latitude, final double radius) {
    return UriComponentsBuilder.fromUriString("/pets/ad")
        .queryParam("longitude", longitude)
        .queryParam("latitude", latitude)
        .queryParam("radius", radius)
        .build()
        .toUriString();
  }

  private static Stream<Arguments> petAdsAndExpectedSearchResultAndUserCoords() {
    // scenario 1
    final SearchArea searchArea1 =
        SearchArea.of(
            List.of(
                List.of(27.417712211608883, 53.88572576837868),
                List.of(27.41878509521484, 53.88322156733548),
                List.of(27.422647476196286, 53.88395513671138),
                List.of(27.421059608459473, 53.88668693702034),
                List.of(27.417712211608883, 53.88572576837868)));
    final PetAd petAd1 = petAdBuilderWithDefaults().searchArea(searchArea1).build();

    final SearchArea searchArea2 =
        SearchArea.of(
            List.of(
                List.of(27.45431900024414, 53.906006796920764),
                List.of(27.45431900024414, 53.90438872212207),
                List.of(27.462987899780273, 53.9038830608939),
                List.of(27.462387084960938, 53.90691693645191),
                List.of(27.45431900024414, 53.906006796920764)));
    final PetAd petAd2 = petAdBuilderWithDefaults().searchArea(searchArea2).build();

    final PetSearchRequest petSearchRequest1 =
        PetSearchRequest.builder()
            .longitude(27.42050170898437)
            .latitude(53.888558623056724)
            .radius(400)
            .build();

    final Arguments scenario1 =
        Arguments.of(List.of(petAd1, petAd2), Set.of(petAd1.getUuid()), petSearchRequest1);

    // scenario 2
    final SearchArea searchArea21 =
        SearchArea.of(
            List.of(
                List.of(27.417068481445312, 53.885826945065915),
                List.of(27.420544624328613, 53.881248454798666),
                List.of(27.4273681640625, 53.884385154154224),
                List.of(27.425780296325684, 53.88805277023041),
                List.of(27.417068481445312, 53.885826945065915)));
    final PetAd petAd21 = petAdBuilderWithDefaults().searchArea(searchArea21).build();

    final SearchArea searchArea22 =
        SearchArea.of(
            List.of(
                List.of(27.42831230163574, 53.89169477394105),
                List.of(27.42865562438965, 53.88828040475983),
                List.of(27.437238693237305, 53.886029297705726),
                List.of(27.44187355041504, 53.88908976193568),
                List.of(27.434964179992676, 53.89536174868413),
                List.of(27.432947158813477, 53.89563991984441),
                List.of(27.42831230163574, 53.89169477394105)));
    final PetAd petAd22 = petAdBuilderWithDefaults().searchArea(searchArea22).build();

    final SearchArea searchArea23 =
        SearchArea.of(
            List.of(
                List.of(27.436981201171875, 53.94350833291436),
                List.of(27.477407455444336, 53.93072520632151),
                List.of(27.481012344360348, 53.943861960581565),
                List.of(27.465391159057614, 53.94345781443147),
                List.of(27.447538375854492, 53.94830730980928),
                List.of(27.436981201171875, 53.94350833291436)));
    final PetAd petAd23 = petAdBuilderWithDefaults().searchArea(searchArea23).build();

    final PetSearchRequest petSearchRequest21 =
        PetSearchRequest.builder()
            .longitude(27.42513656616211)
            .latitude(53.88714221971583)
            .radius(1000)
            .build();

    final Arguments scenario2 =
        Arguments.of(
            List.of(petAd21, petAd22, petAd23),
            Set.of(petAd21.getUuid(), petAd22.getUuid()),
            petSearchRequest21);

    return Stream.of(scenario1, scenario2);
  }
}
