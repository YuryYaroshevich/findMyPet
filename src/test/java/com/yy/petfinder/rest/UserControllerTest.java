package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.PrivateUserView;
import com.yy.petfinder.rest.model.UserUpdate;
import com.yy.petfinder.security.service.TokenService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private UserRepository userRepository;
  @Autowired private TokenService tokenService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
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
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    final String newPhone = "+375298887766";
    final UserUpdate userUpdate = new UserUpdate(newPhone);

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
  }

  @Test
  public void testUpdateUserWithoutTokenUnauthorized() {
    // given
    final UserUpdate userUpdate = new UserUpdate("+375298887766");

    // when then
    webTestClient
        .put()
        .uri("/users")
        .bodyValue(userUpdate)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
