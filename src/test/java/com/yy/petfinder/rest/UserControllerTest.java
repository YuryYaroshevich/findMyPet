package com.yy.petfinder.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
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
    final String email = "abc@email.com";
    final User user = new User(email, "+375296666666");
    userRepository.save(user).block();

    webTestClient
        .get()
        .uri("/users/" + email)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(User.class)
        .isEqualTo(user);
  }

  @Test
  public void testCreateUserSavesUserInDb() {
    final String email = "abc@email.com";
    final User user = new User(email, "+375296666666");

    webTestClient
        .post()
        .uri("/users")
        .syncBody(user)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(User.class)
        .isEqualTo(user);

    assertEquals(Long.valueOf(1), userRepository.count().block());
    assertEquals(user, userRepository.findByEmail(email).block());
  }
}
