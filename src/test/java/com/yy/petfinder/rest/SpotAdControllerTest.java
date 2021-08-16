package com.yy.petfinder.rest;

import static com.yy.petfinder.rest.PetAdController.DEFAULT_PAGE_SIZE;
import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.SpotAdFactory.spotAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static com.yy.petfinder.util.PaginatedResponseHelper.NEXT_PAGE_TOKEN;
import static com.yy.petfinder.util.SearchUriBuilder.getSpotAdsUri;
import static com.yy.petfinder.util.WebTestClientWrapper.getExchange;
import static com.yy.petfinder.util.WebTestClientWrapper.getList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.yy.petfinder.model.*;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.SpotAdRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.*;
import com.yy.petfinder.util.WebTestClientWrapper;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpotAdControllerTest {
  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP)
          .withConfiguration(
              GreenMailConfiguration.aConfig().withUser("petfnder@gmail.com", "pass"))
          .withPerMethodLifecycle(false);

  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private SpotAdRepository spotAdRepository;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
    userRepository.deleteAll().block();
    spotAdRepository.deleteAll().block();
  }

  @Test
  public void testSpotAdCreationNotifiesPetAdsOwners() throws MessagingException {
    // given
    final String email1 = "email1@gmail.com";
    final User user1 = userBuilderWithDefaults().email(email1).build();
    userRepository.save(user1).block();

    final String email2 = "email2@gmail.com";
    final User user2 = userBuilderWithDefaults().email(email2).build();
    userRepository.save(user2).block();

    final String email3 = "email3@gmail.com";
    final User user3 = userBuilderWithDefaults().email(email3).build();
    userRepository.save(user3).block();

    final SearchArea searchArea1 =
        SearchArea.of(
            List.of(
                List.of(27.417068481445312, 53.885826945065915),
                List.of(27.420544624328613, 53.881248454798666),
                List.of(27.4273681640625, 53.884385154154224),
                List.of(27.425780296325684, 53.88805277023041),
                List.of(27.417068481445312, 53.885826945065915)));
    final PetAd petAd1 =
        petAdBuilderWithDefaults()
            .ownerId(user1.getId())
            .searchArea(searchArea1)
            .petType(PetType.DOG)
            .build();
    petAdRepository.save(petAd1).block();

    final SearchArea searchArea2 =
        SearchArea.of(
            List.of(
                List.of(27.42831230163574, 53.89169477394105),
                List.of(27.42865562438965, 53.88828040475983),
                List.of(27.437238693237305, 53.886029297705726),
                List.of(27.44187355041504, 53.88908976193568),
                List.of(27.434964179992676, 53.89536174868413),
                List.of(27.432947158813477, 53.89563991984441),
                List.of(27.42831230163574, 53.89169477394105)));
    final PetAd petAd2 =
        petAdBuilderWithDefaults()
            .ownerId(user2.getId())
            .searchArea(searchArea2)
            .petType(PetType.DOG)
            .build();
    petAdRepository.save(petAd2).block();

    final PetAd petAd3 =
        petAdBuilderWithDefaults()
            .ownerId(user3.getId())
            .searchArea(searchArea2)
            .petType(PetType.CAT)
            .build();
    petAdRepository.save(petAd3).block();

    // when
    final SpotAdView spotAdView =
        SpotAdView.builder()
            .petType(PetType.DOG)
            .description("I've seen this dog next to my house!")
            .phone("375297778899")
            .longitude(27.42513656616211)
            .latitude(53.88714221971583)
            .radius(1000)
            .photoIds(List.of("photo1", "photo2"))
            .emailMessageData(
                EmailMessageData.builder()
                    .subject("Is it your dog?")
                    .text(
                        "Hey, User, this is may be your dog: https://app.com/spotAdView/{spotAdId}")
                    .build())
            .build();

    final SpotAdView spotAdViewCreated =
        webTestClient
            .post()
            .uri("/pets/spotAd")
            .bodyValue(spotAdView)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(SpotAdView.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(1, spotAdRepository.count().block());
    final SpotAd spotAd = spotAdRepository.findAll().blockFirst();
    assertEquals(spotAdView.getPetType(), spotAd.getPetType());
    assertEquals(spotAdView.getDescription(), spotAd.getDescription());
    assertEquals(spotAdView.getPhone(), spotAd.getPhone());
    assertEquals(spotAdView.getLongitude(), spotAd.getPoint().get(0));
    assertEquals(spotAdView.getLatitude(), spotAd.getPoint().get(1));
    assertEquals(spotAdView.getRadius(), spotAd.getRadius());
    assertEquals(spotAdView.getPhotoIds(), spotAd.getPhotoIds());

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    final Set<String> expectedEmails = new HashSet<>();
    expectedEmails.add(email1);
    expectedEmails.add(email2);

    MimeMessage receivedMessage1 = greenMail.getReceivedMessages()[0];
    assertTrue(
        GreenMailUtil.getBody(receivedMessage1)
            .contains(
                "Hey, User, this is may be your dog: https://app.com/spotAdView/"
                    + spotAdViewCreated.getId()));
    assertEquals(1, receivedMessage1.getAllRecipients().length);
    assertTrue(expectedEmails.contains(receivedMessage1.getAllRecipients()[0].toString()));
    expectedEmails.remove(receivedMessage1.getAllRecipients()[0].toString());

    MimeMessage receivedMessage2 = greenMail.getReceivedMessages()[1];
    assertTrue(
        GreenMailUtil.getBody(receivedMessage2)
            .contains(
                "Hey, User, this is may be your dog: https://app.com/spotAdView/"
                    + spotAdViewCreated.getId()));
    assertEquals(1, receivedMessage2.getAllRecipients().length);
    assertTrue(expectedEmails.contains(receivedMessage2.getAllRecipients()[0].toString()));
    expectedEmails.remove(receivedMessage2.getAllRecipients()[0].toString());

    assertEquals(0, expectedEmails.size());
  }

  @Test
  public void testGetSpotAdReturnsCorrectly() {
    // given
    final SpotAd spotAd =
        SpotAd.builder()
            .id(new ObjectId().toHexString())
            .description("I've seen this dog next to my house")
            .phone("375297778899")
            .point(List.of(27.42513656616211, 53.88714221971583))
            .radius(40000)
            .photoIds(List.of("photo1"))
            .petType(PetType.DOG)
            .createdAt(Instant.now())
            .build();
    spotAdRepository.save(spotAd).block();

    // when
    final SpotAdResponse spotAdResponse =
        WebTestClientWrapper.get(
            webTestClient, "/pets/spotAd/" + spotAd.getId(), SpotAdResponse.class);

    // then
    assertEquals(spotAd.getId(), spotAdResponse.getId());
    assertEquals(spotAd.getDescription(), spotAdResponse.getDescription());
    assertEquals(spotAd.getPhone(), spotAdResponse.getPhone());
    assertEquals(spotAd.getPoint().get(0), spotAdResponse.getLongitude());
    assertEquals(spotAd.getPoint().get(1), spotAdResponse.getLatitude());
    assertEquals(spotAd.getPetType(), spotAdResponse.getPetType());
    assertEquals(spotAd.getRadius(), spotAdResponse.getRadius());
    assertEquals(spotAd.getPhotoIds(), spotAdResponse.getPhotoIds());
    assertEquals(
        spotAd.getCreatedAt().toEpochMilli(), spotAdResponse.getCreatedAt().toEpochMilli());
  }

  @Test
  public void testGetSpotAdReturns404IfNotFound() {
    // given
    final String spotAdId = new ObjectId().toHexString();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .get()
            .uri("/pets/spotAd/" + spotAdId)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "SpotAd with provided id not found: id=" + spotAdId;
    assertEquals(errorMsg, errorResp.get("message"));
  }

  @Test
  public void testGetSpotAdsReturnsRelevantAds() {
    // given
    final SpotAd spotAd1 =
        spotAdBuilderWithDefaults()
            // Minsk
            .point(List.of(27.429428100585938, 53.874291225365305))
            .build();
    spotAdRepository.save(spotAd1).block();

    final SpotAd spotAd2 =
        spotAdBuilderWithDefaults()
            // Minsk
            .point(List.of(27.432174682617184, 53.87165982602082))
            .build();
    spotAdRepository.save(spotAd2).block();

    final SpotAd spotAd3 =
        spotAdBuilderWithDefaults()
            // Warsaw
            .point(List.of(21.02783203125, 52.217704193421454))
            .build();
    spotAdRepository.save(spotAd3).block();

    final SpotAdRequest spotAdRequest =
        SpotAdRequest.builder()
            .petType(PetType.DOG)
            .radius(40000)
            .latitude(53.93365607146903)
            .longitude(27.62889862060547)
            .build();

    // when
    final List<SpotAdResponse> spotAds =
        WebTestClientWrapper.getList(
            webTestClient, getSpotAdsUri(spotAdRequest), SpotAdResponse.class);

    // then
    assertEquals(2, spotAds.size());
    assertEquals(spotAd2.getId(), spotAds.get(0).getId());
    assertEquals(spotAd1.getId(), spotAds.get(1).getId());
  }

  @Test
  public void testGetSpotAdsPagingWorksCorrectly() {
    // given
    final SpotAd spotAd1 =
        spotAdBuilderWithDefaults().point(List.of(27.429428100585938, 53.874291225365305)).build();
    spotAdRepository.save(spotAd1).block();

    final SpotAd spotAd2 =
        spotAdBuilderWithDefaults().point(List.of(27.432174682617184, 53.87165982602082)).build();
    spotAdRepository.save(spotAd2).block();

    final SpotAd spotAd3 =
        spotAdBuilderWithDefaults().point(List.of(27.47852325439453, 53.879654718999255)).build();
    spotAdRepository.save(spotAd3).block();

    final SpotAdRequest spotAdRequest =
        SpotAdRequest.builder()
            .petType(PetType.DOG)
            .radius(40000)
            .latitude(53.93365607146903)
            .longitude(27.62889862060547)
            .build();

    // when
    final EntityExchangeResult<List<SpotAdResponse>> response =
        getExchange(
            webTestClient, getSpotAdsUri(spotAdRequest, new Paging(2)), SpotAdResponse.class);
    final List<SpotAdResponse> spotAds1 = response.getResponseBody();
    assertEquals(2, spotAds1.size());
    assertEquals(spotAd3.getId(), spotAds1.get(0).getId());
    assertEquals(spotAd2.getId(), spotAds1.get(1).getId());
    final String nextPageToken = response.getResponseHeaders().get(NEXT_PAGE_TOKEN).get(0);
    assertEquals(spotAd2.getId(), nextPageToken);

    final EntityExchangeResult<List<SpotAdResponse>> response2 =
        getExchange(
            webTestClient,
            getSpotAdsUri(spotAdRequest, new Paging(nextPageToken, 2)),
            SpotAdResponse.class);
    final List<SpotAdResponse> spotAds2 = response2.getResponseBody();
    assertEquals(1, spotAds2.size());
    assertEquals(spotAd1.getId(), spotAds2.get(0).getId());
    assertNull(response2.getResponseHeaders().get(NEXT_PAGE_TOKEN));
  }

  @Test
  public void testDefaultPageSizeWorks() {
    final List<SpotAd> spotAds =
        IntStream.range(0, 30).mapToObj(i -> spotAdBuilderWithDefaults().build()).collect(toList());
    spotAdRepository.saveAll(spotAds).blockLast();

    final SpotAdRequest spotAdRequest =
        SpotAdRequest.builder()
            .petType(PetType.DOG)
            .radius(40000)
            .latitude(53.93365607146903)
            .longitude(27.62889862060547)
            .build();

    final List<SpotAdResponse> spotAdResponses =
        getList(webTestClient, getSpotAdsUri(spotAdRequest), SpotAdResponse.class);

    assertEquals(DEFAULT_PAGE_SIZE, spotAdResponses.size());
  }
}
