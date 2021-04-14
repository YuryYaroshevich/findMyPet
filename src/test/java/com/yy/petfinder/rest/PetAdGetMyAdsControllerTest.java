package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.security.service.TokenService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = MailSenderValidatorAutoConfiguration.class)
public class PetAdGetMyAdsControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private PetAdRepository petAdRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private TokenService tokenService;

  private String authHeaderValue1;
  private String userId1;

  private String authHeaderValue2;
  private String userId2;

  @BeforeEach
  public void setup() {
    petAdRepository.deleteAll().block();
    userRepository.deleteAll().block();
    final User user1 = userBuilderWithDefaults().build();
    userRepository.save(user1).block();
    authHeaderValue1 = "Bearer " + tokenService.createToken(user1.getId());
    userId1 = user1.getId();

    final User user2 = userBuilderWithDefaults().build();
    userRepository.save(user1).block();
    authHeaderValue2 = "Bearer " + tokenService.createToken(user2.getId());
    userId2 = user2.getId();
  }

  @Test
  public void testGetMyPetAdsReturnsCorrectAds() {
    // given
    final PetAd petAd1 = petAdBuilderWithDefaults().ownerId(userId1).build();
    final PetAd petAd2 = petAdBuilderWithDefaults().ownerId(userId1).build();

    final PetAd petAd3 = petAdBuilderWithDefaults().ownerId(userId2).build();
    final PetAd petAd4 = petAdBuilderWithDefaults().ownerId(userId2).build();

    petAdRepository.saveAll(List.of(petAd1, petAd2, petAd3, petAd4)).blockLast();

    // when
    final List<PetAdView> petAdViews =
        webTestClient
            .get()
            .uri("/pets/user/ad")
            .header(AUTHORIZATION, authHeaderValue1)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(PetAdView.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(2, petAdViews.size());
    assertEquals(petAd1.getId(), petAdViews.get(0).getId());
    assertEquals(petAd2.getId(), petAdViews.get(1).getId());
  }
}
