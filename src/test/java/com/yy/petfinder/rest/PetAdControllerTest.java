package com.yy.petfinder.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.model.SearchArea;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
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
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();
    final SearchArea searchArea =
        SearchArea.of(
            List.of(
                List.of(53.911665, 27.469369),
                List.of(53.911867, 27.491685),
                List.of(53.899226, 27.491856),
                List.of(53.897405, 27.461129)));
    final PetType petType = PetType.DOG;
    final String name = "Fido";
    final String ownerId = UUID.randomUUID().toString();
    final byte[] imageBlob = {1, 2, 3};
    final String color = "black";

    final PetAd petAd =
        PetAd.builder()
            .id(objectId)
            .uuid(uuid)
            .searchArea(searchArea)
            .petType(petType)
            .name(name)
            .ownerId(ownerId)
            .imageBlob(imageBlob)
            .color(color)
            .build();
    final PetAdView expectedPetAd =
        PetAdView.builder()
            .uuid(uuid)
            .searchArea(new SearchAreaView(searchArea.getCoordinates()))
            .petType(petType)
            .name(name)
            .ownerId(ownerId)
            .imageBlob(imageBlob)
            .color(color)
            .build();

    petAdRepository.save(petAd).block();

    final PetAdView petAdView =
        webTestClient
            .get()
            .uri("/pets/ad/" + uuid)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PetAdView.class)
            .returnResult()
            .getResponseBody();

    assertEquals(expectedPetAd, petAdView);
  }

  @Test
  public void fuck() {
    List<List<Double>> list = List.of(List.of(1.0, 2.0));
    List<List<Double>> list2 = List.of(List.of(1.0, 2.0));

    assertEquals(list, list2);
  }
}
