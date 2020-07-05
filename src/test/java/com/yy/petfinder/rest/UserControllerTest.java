package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.PrivateUserView;
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

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
  }

  @Test
  public void testGetUserReturnCorrectUser() {
    final User user = userBuilderWithDefaults().build();
    final PrivateUserView expectedUser =
        new PrivateUserView(user.getId(), user.getEmail(), user.getPhone());
    userRepository.save(user).block();

    final PrivateUserView createdUser =
        webTestClient
            .get()
            .uri("/users/" + user.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(PrivateUserView.class)
            .returnResult()
            .getResponseBody();

    assertEquals(expectedUser, createdUser);
  }
}
