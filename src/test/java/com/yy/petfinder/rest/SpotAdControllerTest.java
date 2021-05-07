package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                    .text("Hey, User, this is may be your dog.")
                    .build())
            .build();

    webTestClient
        .post()
        .uri("/pets/spotAd")
        .bodyValue(spotAdView)
        .exchange()
        .expectStatus()
        .isCreated();

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
        GreenMailUtil.getBody(receivedMessage1).equals(spotAdView.getEmailMessageData().getText()));
    assertEquals(1, receivedMessage1.getAllRecipients().length);
    assertTrue(expectedEmails.contains(receivedMessage1.getAllRecipients()[0].toString()));
    expectedEmails.remove(receivedMessage1.getAllRecipients()[0].toString());

    MimeMessage receivedMessage2 = greenMail.getReceivedMessages()[1];
    assertTrue(
        GreenMailUtil.getBody(receivedMessage2).equals(spotAdView.getEmailMessageData().getText()));
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
}
