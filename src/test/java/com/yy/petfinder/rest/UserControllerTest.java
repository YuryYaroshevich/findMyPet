package com.yy.petfinder.rest;

import static com.yy.petfinder.rest.model.Messenger.TELEGRAM;
import static com.yy.petfinder.rest.model.Messenger.VIBER;
import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.yy.petfinder.model.User;
import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.Messenger;
import com.yy.petfinder.rest.model.PasswordUpdate;
import com.yy.petfinder.rest.model.PasswordUpdateEmail;
import com.yy.petfinder.rest.model.PasswordUpdateRequest;
import com.yy.petfinder.rest.model.PrivateUserView;
import com.yy.petfinder.rest.model.UserUpdate;
import com.yy.petfinder.security.service.TokenService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
  @Autowired private UserRandomKeyRepository userRandomKeyRepository;
  @Autowired private TokenService tokenService;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
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
            .build();

    // when
    webTestClient
        .mutate().responseTimeout(Duration.ofMillis(10000)).build()
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
            .startsWith(
                "To reset your password click the following link: http://localhost:3000?key"));
    assertTrue(GreenMailUtil.getBody(receivedMessage).contains(user.getId()));
    assertEquals(1, receivedMessage.getAllRecipients().length);
    assertEquals(user.getEmail(), receivedMessage.getAllRecipients()[0].toString());
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
}
