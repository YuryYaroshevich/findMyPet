package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.util.SearchUriBuilder.searchUri;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Test
  public void testSearchPetByPetTypeWorksCorrectly() {
    // given
    final PetAd dogAd = petAdBuilderWithDefaults().petType(PetType.DOG).build();
    final PetAd catAd = petAdBuilderWithDefaults().petType(PetType.CAT).build();

    petAdRepository.saveAll(List.of(dogAd, catAd)).blockLast();

    // when
    final List<PetAdView> petAdViews =
        webTestClient
            .get()
            .uri(searchUri(27.417068481445312, 53.885826945065915, 400, PetType.DOG))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PetAdView.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(1, petAdViews.size());
    assertEquals(dogAd.getUuid(), petAdViews.get(0).getUuid());
  }
}
