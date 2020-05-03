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
                List.of(53.885875, 27.416621),
                List.of(53.887292, 27.429238),
                List.of(53.881069, 27.419711),
                List.of(53.882435, 27.431384)));
    final PetAd petAd1 = petAdBuilderWithDefaults().searchArea(searchArea1).build();

    final SearchArea searchArea2 =
        SearchArea.of(
            List.of(
                List.of(53.906200, 27.436632),
                List.of(53.905381, 27.451759),
                List.of(53.900034, 27.451105),
                List.of(53.900757, 27.437941)));
    final PetAd petAd2 = petAdBuilderWithDefaults().searchArea(searchArea2).build();

    petAdRepository.save(petAd1).block();
    petAdRepository.save(petAd2).block();

    final String uri =
        UriComponentsBuilder.fromUriString("/pets/ad")
            .queryParam("longitude", 53.889052)
            .queryParam("latitude", 27.419605)
            .queryParam("radius", 417.94)
            .build()
            .toUriString();
    final List<PetAdView> petAds =
        webTestClient
            .get()
            .uri(uri)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(List.class)
            .returnResult()
            .getResponseBody();

    assertEquals(1, petAds.size());
    assertEquals(petAd1.getUuid(), petAds.get(0).getUuid());
  }
}
