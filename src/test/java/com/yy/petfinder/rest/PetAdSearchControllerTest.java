package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdSearchControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;

  @Autowired private ReactiveMongoTemplate mongoTemplate;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
  }

  @Test
  public void testSearchPetReturnsAllAdsNearProvidedCoords() {

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

    petAdRepository.save(petAd1).block();
    petAdRepository.save(petAd2).block();

    final List<PetAdView> petAds =
        webTestClient
            .get()
            .uri(searchUri(27.42050170898437, 53.888558623056724, 400))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PetAdView.class)
            .returnResult()
            .getResponseBody();

    assertEquals(1, petAds.size());
    assertEquals(petAd1.getUuid(), petAds.get(0).getUuid());
  }

  private String searchUri(final double longitude, final double latitude, final double radius) {
    return UriComponentsBuilder.fromUriString("/pets/ad")
        .queryParam("longitude", longitude)
        .queryParam("latitude", latitude)
        .queryParam("radius", radius)
        .build()
        .toUriString();
  }
}
