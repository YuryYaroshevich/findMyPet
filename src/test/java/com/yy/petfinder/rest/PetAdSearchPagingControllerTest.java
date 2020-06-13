package com.yy.petfinder.rest;

import static com.yy.petfinder.rest.PetAdController.NEXT_PAGE_TOKEN;
import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.util.SearchUriBuilder.searchUri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.PetSearchRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetAdSearchPagingControllerTest {
  private static final int PAGE_SIZE = 2;

  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
  }

  @Test
  public void testPagingWorks() {
    final PetAd petAd1 = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd1).block();
    final PetAd petAd2 = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd2).block();
    final PetAd petAd3 = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd3).block();
    final PetAd petAd4 = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd4).block();
    final PetAd petAd5 = petAdBuilderWithDefaults().build();
    petAdRepository.save(petAd5).block();

    final PetSearchRequest petSearchReq =
        PetSearchRequest.builder()
            .longitude(27.417068481445312)
            .latitude(53.885826945065915)
            .radius(400)
            .petType(PetType.DOG)
            .build();

    final Paging paging = new Paging(null, PAGE_SIZE); // Paging.builder().pageSize(2).build();

    final EntityExchangeResult<List<PetAdView>> response =
        searchPetAdsResponse(petSearchReq, paging);
    final List<PetAdView> petAdViews = response.getResponseBody();
    assertEquals(2, petAdViews.size());
    assertEquals(petAd5.getId(), petAdViews.get(0).getId());
    assertEquals(petAd4.getId(), petAdViews.get(1).getId());
    final String nextPageToken = response.getResponseHeaders().get(NEXT_PAGE_TOKEN).get(0);
    assertEquals(petAd4.getId(), nextPageToken);

    final Paging paging2 =
        new Paging(
            nextPageToken,
            PAGE_SIZE); // Paging.builder().nextPageToken(nextPageToken).pageSize(2).build();

    final EntityExchangeResult<List<PetAdView>> response2 =
        searchPetAdsResponse(petSearchReq, paging2);
    final List<PetAdView> petAdViews2 = response2.getResponseBody();
    assertEquals(2, petAdViews2.size());
    assertEquals(petAd3.getId(), petAdViews2.get(0).getId());
    assertEquals(petAd2.getId(), petAdViews2.get(1).getId());
    final String nextPageToken2 = response2.getResponseHeaders().get(NEXT_PAGE_TOKEN).get(0);
    assertEquals(petAd2.getId(), nextPageToken2);

    final Paging paging3 =
        new Paging(
            nextPageToken2,
            PAGE_SIZE); // Paging.builder().nextPageToken(nextPageToken2).pageSize(2).build();

    final EntityExchangeResult<List<PetAdView>> response3 =
        searchPetAdsResponse(petSearchReq, paging3);
    final List<PetAdView> petAdViews3 = response3.getResponseBody();
    assertEquals(1, petAdViews3.size());
    assertEquals(petAd1.getId(), petAdViews3.get(0).getId());

    assertFalse(response3.getResponseHeaders().containsKey(NEXT_PAGE_TOKEN));
  }

  private EntityExchangeResult<List<PetAdView>> searchPetAdsResponse(
      final PetSearchRequest petSearchReq, final Paging paging) {
    return webTestClient
        .get()
        .uri(searchUri(petSearchReq, paging))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PetAdView.class)
        .returnResult();
  }
}
