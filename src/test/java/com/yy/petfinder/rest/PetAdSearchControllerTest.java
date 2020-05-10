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
              List.of(27.417712211608883,
                53.88572576837868),
              List.of(27.41878509521484,
                53.88322156733548),
              List.of(27.422647476196286,
                53.88395513671138),
              List.of(27.421059608459473,
                53.88668693702034),
              List.of(27.417712211608883,
                53.88572576837868)
                /*List.of(27.416621, 53.885875),
                List.of(27.429238, 53.887292),
                List.of(27.419711, 53.881069),
                List.of(27.431384, 53.882435),
                List.of(27.416621, 53.885875)*/));
    final PetAd petAd1 = petAdBuilderWithDefaults().searchArea(searchArea1).build();

    final SearchArea searchArea2 =
        SearchArea.of(
            List.of(
              List.of(27.45431900024414,
                53.906006796920764),
              List.of(27.45431900024414,
                53.90438872212207),
              List.of(27.462987899780273,
                53.9038830608939),
              List.of(27.462387084960938,
                53.90691693645191),
              List.of(27.45431900024414,
                53.906006796920764)
                /*List.of(27.436632, 53.906200),
                List.of(27.451759, 53.905381),
                List.of(27.451105, 53.900034),
                List.of(27.437941, 53.900757),
                List.of(27.436632, 53.906200)*/));
    final PetAd petAd2 = petAdBuilderWithDefaults().searchArea(searchArea2).build();

    petAdRepository.save(petAd1).block();
    petAdRepository.save(petAd2).block();

    final String uri =
        UriComponentsBuilder.fromUriString("/pets/ad")
            .queryParam("longitude", 27.42050170898437)
            .queryParam("latitude", 53.888558623056724)
            .queryParam("radius", 0.00001)
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
