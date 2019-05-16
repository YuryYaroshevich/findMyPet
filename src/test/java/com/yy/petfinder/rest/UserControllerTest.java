package com.yy.petfinder.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import java.util.UUID;
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

  private static final String USER_ID = UUID.randomUUID().toString();

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
  }

  @Test
  public void testGetUserReturnCorrectUser() {
    final String email = "abc@email.com";
    final User user = new User(USER_ID, email, "+375296666666");
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
    final User expectedUser = new User(USER_ID, email, "+375296666666");

    webTestClient.post().uri("/users").syncBody(expectedUser).exchange().expectStatus().isCreated();

    assertEquals(Long.valueOf(1), userRepository.count().block());
    final User user = userRepository.findByEmail(email).block();
    assertEquals(expectedUser.getEmail(), user.getEmail());
  }

  @Test
  public void testCreateUserReturnsErrorIfEmailExists() {
    final String email = "abc@email.com";
    final User user = new User(USER_ID, email, "+375296666666");
    userRepository.save(user).block();

    webTestClient.post().uri("/users").syncBody(user).exchange().expectStatus().is5xxServerError();
  }
}
