package com.yy.petfinder.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.UserView;
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
    final String password = "1234";
    final String phone = "+375296666666";
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();
    final User user =
        User.builder().id(objectId).uuid(uuid).email(email).phone(phone).password(password).build();
    final UserView expectedUser = new UserView(uuid, email, phone);
    userRepository.save(user).block();

    final UserView createdUser =
        webTestClient
            .get()
            .uri("/users/" + uuid)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(UserView.class)
            .returnResult()
            .getResponseBody();

    assertEquals(expectedUser, createdUser);
  }

  @Test
  public void testCreateUserSavesUserInDb() {
    final String email = "abc@email.com";
    final String password = "xyz";
    final String phone = "+375296666666";
    final CreateUser newUser = new CreateUser(email, phone, password);

    webTestClient.post().uri("/users").bodyValue(newUser).exchange().expectStatus().isCreated();

    assertEquals(Long.valueOf(1), userRepository.count().block());
    final List<User> users = userRepository.findAll().collectList().block();
    assertEquals(1, users.size());

    final User createdUser = users.get(0);
    assertEquals(email, createdUser.getEmail());
    assertEquals(password, createdUser.getPassword());
    assertEquals(phone, createdUser.getPhone());
  }
}
