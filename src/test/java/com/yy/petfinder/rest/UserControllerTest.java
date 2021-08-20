package com.yy.petfinder.rest;

import static com.yy.petfinder.model.PetAdResult.REMOVED_WITH_PROFILE;
import static com.yy.petfinder.model.User.PASSWORD_PLACEHOLDER;
import static com.yy.petfinder.rest.model.Messenger.TELEGRAM;
import static com.yy.petfinder.rest.model.Messenger.VIBER;
import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.yy.petfinder.model.*;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.PetAdResolutionRepository;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.*;
import com.yy.petfinder.security.service.TokenService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP)
          .withConfiguration(
              GreenMailConfiguration.aConfig().withUser("petfnder@gmail.com", "pass"))
          .withPerMethodLifecycle(false);

  @Autowired private WebTestClient webTestClient;

  @Autowired private UserRepository userRepository;
  @Autowired private PetAdRepository petAdRepository;
  @Autowired private PetAdResolutionRepository petAdResolutionRepository;
  @Autowired private UserRandomKeyRepository userRandomKeyRepository;
  @Autowired private TokenService tokenService;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
    petAdRepository.deleteAll().block();
    petAdResolutionRepository.deleteAll().block();
    userRandomKeyRepository.deleteAll().block();
  }

  @Test
  public void testGetUserPublicReturnCorrectUser() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();

    // when
    final Map<String, String> fetchedUser =
        webTestClient
            .get()
            .uri("/users/" + user.getId() + "/public")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(user.getId(), fetchedUser.get("id"));
    assertEquals(user.getPhone(), fetchedUser.get("phone"));
    assertFalse(fetchedUser.containsKey("email"));
  }

  @Test
  public void testGetUserPublicReturnNotFoundIfNoUser() {
    // given
    final String userId = new ObjectId().toHexString();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .get()
            .uri("/users/" + userId + "/public")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "User with provided id not found: id=" + userId;
    assertEquals(errorMsg, errorResp.get("message"));
  }

  @Test
  public void testGetUserPrivateReturnCorrectUser() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    final PrivateUserView expectedUser =
        PrivateUserView.builder()
            .id(user.getId())
            .email(user.getEmail())
            .phone(user.getPhone())
            .messengers(user.getMessengers())
            .build();

    // when
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    final PrivateUserView createdUser =
        webTestClient
            .get()
            .uri("/users/private")
            .header(AUTHORIZATION, authHeaderValue)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PrivateUserView.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(expectedUser, createdUser);
  }

  @Test
  public void testGetUserPrivateWithoutTokenUnauthorized() {
    webTestClient.get().uri("/users/private").exchange().expectStatus().isUnauthorized();
  }

  @Test
  public void testUpdateUserUpdatesUserData() {
    // given
    final String oldPassword = "1234";
    final String encodedOldPassword = passwordEncoder.encode(oldPassword);
    final User user = userBuilderWithDefaults().password(encodedOldPassword).build();
    userRepository.save(user).block();

    final String newPhone = "+375298887766";
    final List<Messenger> messengers = List.of(TELEGRAM, VIBER);
    final String newPassword = "5678";
    final UserUpdate userUpdate =
        UserUpdate.builder()
            .phone(newPhone)
            .messengers(messengers)
            .passwordUpdate(new PasswordUpdate(newPassword, oldPassword))
            .build();

    // when
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    webTestClient
        .put()
        .uri("/users")
        .bodyValue(userUpdate)
        .header(AUTHORIZATION, authHeaderValue)
        .exchange()
        .expectStatus()
        .isOk();

    // then
    final User updatedUser = userRepository.findById(user.getId()).block();
    assertEquals(user.getEmail(), updatedUser.getEmail());
    assertEquals(newPhone, updatedUser.getPhone());
    assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
  }

  @Test
  public void testUpdateUserReturns400IfUserProvidesInvalidPassword() {
    // given
    final String oldPassword = "1234";
    final String encodedOldPassword = passwordEncoder.encode(oldPassword);
    final User user = userBuilderWithDefaults().password(encodedOldPassword).build();
    userRepository.save(user).block();

    final String newPhone = "+375298887766";
    final List<Messenger> messengers = List.of(TELEGRAM, VIBER);
    final String newPassword = "5678";
    final UserUpdate userUpdate =
        UserUpdate.builder()
            .phone(newPhone)
            .messengers(messengers)
            .passwordUpdate(new PasswordUpdate(newPassword, "invalidOldPass"))
            .build();

    // when then
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    webTestClient
        .put()
        .uri("/users")
        .bodyValue(userUpdate)
        .header(AUTHORIZATION, authHeaderValue)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  public void testUpdatePasswordReturns404ForOAuth2User() {
    // given
    final User user =
        User.builder()
            .id(new ObjectId().toHexString())
            .email("foobar@gmail.com")
            .password(PASSWORD_PLACEHOLDER)
            .oAuth2Provider(OAuth2Provider.GOOGLE)
            .build();
    userRepository.save(user).block();
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());

    final UserUpdate userUpdate =
        UserUpdate.builder()
            .phone("375294443322")
            .messengers(List.of(TELEGRAM))
            .passwordUpdate(new PasswordUpdate("newPassword", "invalidOldPass"))
            .build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .put()
            .uri("/users")
            .bodyValue(userUpdate)
            .header(AUTHORIZATION, authHeaderValue)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    assertEquals("User was created with oauth2 provider(Google)", errorResp.get("message"));
  }

  @Test
  public void testEmptyUpdateUserDoesntSpoilUserData() {
    // given
    final User user =
        User.builder()
            .id(new ObjectId().toHexString())
            .email("foobar@gmail.com")
            .password(PASSWORD_PLACEHOLDER)
            .oAuth2Provider(OAuth2Provider.GOOGLE)
            .build();
    userRepository.save(user).block();
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());

    final UserUpdate userUpdate = UserUpdate.builder().build();

    // when
    final PrivateUserView userView =
        webTestClient
            .put()
            .uri("/users")
            .bodyValue(userUpdate)
            .header(AUTHORIZATION, authHeaderValue)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PrivateUserView.class)
            .returnResult()
            .getResponseBody();

    // then
    assertEquals(user.getId(), userView.getId());
    assertEquals(user.getEmail(), userView.getEmail());
  }

  @Test
  public void testUpdateUserWithoutTokenUnauthorized() {
    // given
    final UserUpdate userUpdate = UserUpdate.builder().phone("+375298887766").build();

    // when then
    webTestClient
        .put()
        .uri("/users")
        .bodyValue(userUpdate)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @DisplayName("sends email and stores token in db")
  @Test
  public void testSendNewPasswordEmailWorksCorrectly() throws MessagingException {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    final PasswordUpdateEmail passwordUpdateEmail =
        PasswordUpdateEmail.builder()
            .email(user.getEmail())
            .frontendHost("http://localhost:3000")
            .emailMessageData(
                EmailMessageData.builder()
                    .subject("Reset password")
                    .text("To reset your password click the following link: {link}")
                    .build())
            .build();

    // when
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofMillis(10000))
        .build()
        .post()
        .uri("/users/newPasswordEmail")
        .bodyValue(passwordUpdateEmail)
        .exchange()
        .expectStatus()
        .isOk();

    // then
    final UserRandomKey userRandomKey = userRandomKeyRepository.findById(user.getId()).block();
    assertEquals(user.getId(), userRandomKey.getId());

    MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
    assertTrue(
        GreenMailUtil.getBody(receivedMessage)
            .contains(
                "To reset your password click the following link: http://localhost:3000?key"));
    assertTrue(GreenMailUtil.getBody(receivedMessage).contains(user.getId()));
    assertEquals(1, receivedMessage.getAllRecipients().length);
    assertEquals(user.getEmail(), receivedMessage.getAllRecipients()[0].toString());
  }

  @Test
  public void testEmailNotSentIfUserDoesNotExist() {
    // given
    final String email = "foobar@gmail.com";
    final PasswordUpdateEmail passwordUpdateEmail =
        PasswordUpdateEmail.builder()
            .email(email)
            .frontendHost("http://localhost:3000")
            .emailMessageData(
                EmailMessageData.builder()
                    .subject("Reset password")
                    .text("To reset your password click the following link: {link}")
                    .build())
            .build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/users/newPasswordEmail")
            .bodyValue(passwordUpdateEmail)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "User with provided email not found: email=" + email;
    assertEquals(errorMsg, errorResp.get("message"));

    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @Test
  public void testEmailNotSentIfUserOAuth2Authorized() {
    // given
    final String email = "foobar@gmail.com";
    final User user =
        User.builder()
            .id(new ObjectId().toHexString())
            .email(email)
            .password(PASSWORD_PLACEHOLDER)
            .oAuth2Provider(OAuth2Provider.GOOGLE)
            .build();
    userRepository.save(user).block();

    final PasswordUpdateEmail passwordUpdateEmail =
        PasswordUpdateEmail.builder()
            .email(email)
            .frontendHost("http://localhost:3000")
            .emailMessageData(
                EmailMessageData.builder()
                    .subject("Reset password")
                    .text("To reset your password click the following link: {link}")
                    .build())
            .build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/users/newPasswordEmail")
            .bodyValue(passwordUpdateEmail)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String errorMsg = "User was created with oauth2 provider(Google)";
    assertEquals(errorMsg, errorResp.get("message"));

    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @DisplayName("sets new password if userId and key are correct")
  @Test
  public void testNewPasswordSetsNewPassword() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();

    final String randomKey = UUID.randomUUID().toString();
    final UserRandomKey userRandomKey =
        UserRandomKey.builder()
            .id(user.getId())
            .randomKey(randomKey)
            .createdAt(Instant.now())
            .build();
    userRandomKeyRepository.save(userRandomKey).block();

    final String newPassword = "newPassword";
    final PasswordUpdateRequest passwordUpdateRequest =
        PasswordUpdateRequest.builder()
            .userId(user.getId())
            .key(randomKey)
            .newPassword(newPassword)
            .build();

    // when
    webTestClient
        .put()
        .uri("/users/newPassword")
        .bodyValue(passwordUpdateRequest)
        .exchange()
        .expectStatus()
        .isOk();

    // then
    final User userWithNewPassword = userRepository.findById(user.getId()).block();
    assertTrue(passwordEncoder.matches(newPassword, userWithNewPassword.getPassword()));
  }

  @DisplayName("returns unauthorized if recovery key doesn't exist")
  @Test
  public void testNewPasswordReturnsUnauthorized() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();

    final String randomKey = UUID.randomUUID().toString();
    final String newPassword = "newPassword";
    final PasswordUpdateRequest passwordUpdateRequest =
        PasswordUpdateRequest.builder()
            .userId(user.getId())
            .key(randomKey)
            .newPassword(newPassword)
            .build();

    // when
    final Map<String, String> responseBody =
        webTestClient
            .put()
            .uri("/users/newPassword")
            .bodyValue(passwordUpdateRequest)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    assertTrue(
        responseBody.get("message").contains("Password recovery request contains invalid token"));
  }

  @Test
  public void testDeleteUserRemovesUserAndHerPetAds() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();

    final PetAd petAd1 = petAdBuilderWithDefaults().ownerId(user.getId()).build();
    petAdRepository.save(petAd1).block();

    final PetAd petAd2 = petAdBuilderWithDefaults().ownerId(user.getId()).build();
    petAdRepository.save(petAd2).block();

    // when
    final String authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    webTestClient
        .get()
        .uri("/users/private")
        .header(AUTHORIZATION, authHeaderValue)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .delete()
        .uri("/users")
        .header(AUTHORIZATION, authHeaderValue)
        .exchange()
        .expectStatus()
        .isOk();

    // then
    assertNull(userRepository.findById(user.getId()).block());
    assertNull(petAdRepository.findById(petAd1.getId()).block());
    assertNull(petAdRepository.findById(petAd2.getId()).block());

    final List<PetAdResolution> petAdResolutions =
        petAdResolutionRepository.findAll().collectList().block();
    assertEquals(2, petAdResolutions.size());
    final PetAdResolution resolution1 =
        petAdResolutions.stream()
            .filter(resolution -> resolution.getId().equals(petAd1.getId()))
            .findFirst()
            .get();
    assertEquals(REMOVED_WITH_PROFILE, resolution1.getPetAdResult());
    final PetAdResolution resolution2 =
        petAdResolutions.stream()
            .filter(resolution -> resolution.getId().equals(petAd2.getId()))
            .findFirst()
            .get();
    assertEquals(REMOVED_WITH_PROFILE, resolution2.getPetAdResult());

    webTestClient
        .get()
        .uri("/users/private")
        .header(AUTHORIZATION, authHeaderValue)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
