package com.yy.petfinder.rest;

import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yy.petfinder.model.OAuth2Provider;
import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.Login;
import com.yy.petfinder.rest.model.Messenger;
import com.yy.petfinder.security.model.JWTToken;
import com.yy.petfinder.security.service.TokenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = MailSenderValidatorAutoConfiguration.class)
public class AuthControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private TokenService tokenService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll().block();
  }

  @Test
  public void testSignUpSavesUserInDb() {
    // given
    final String email = "abc@email.com";
    final String password = "xyz";
    final String phone = "+375296666666";
    final List<Messenger> messengers = List.of(Messenger.TELEGRAM, Messenger.VIBER);
    final CreateUser newUser = new CreateUser(email, phone, password, messengers);

    // when
    webTestClient.post().uri("/signUp").bodyValue(newUser).exchange().expectStatus().isCreated();

    // then
    assertEquals(Long.valueOf(1), userRepository.count().block());
    final List<User> users = userRepository.findAll().collectList().block();
    assertEquals(1, users.size());

    final User createdUser = users.get(0);
    assertEquals(email, createdUser.getEmail());
    assertTrue(passwordEncoder.matches(password, createdUser.getPassword()));
    assertEquals(phone, createdUser.getPhone());
    assertEquals(messengers, createdUser.getMessengers());
  }

  @Test
  public void testSignUpReturnsConflictIfUserWithSameEmailExistsInDb() {
    // given
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    final CreateUser newUser =
        new CreateUser(user.getEmail(), user.getPhone(), "1234", List.of(Messenger.VIBER));

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/signUp")
            .bodyValue(newUser)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    final String expectedError = "User with such email already exists: email=" + user.getEmail();
    assertEquals(expectedError, errorResp.get("message"));
  }

  @Test
  public void testLoginReturnsValidToken() {
    // given
    final String password = "1234";
    final User user = userBuilderWithDefaults().password(passwordEncoder.encode(password)).build();
    userRepository.save(user).block();
    final Login login = Login.builder().email(user.getEmail()).password(password).build();

    // when
    final JWTToken token =
        webTestClient
            .post()
            .uri("/login")
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(JWTToken.class)
            .returnResult()
            .getResponseBody();

    assertEquals(user.getId(), tokenService.getUserIdFromToken(token.getToken()));
  }

  @Test
  public void testLoginReturnsUnauthorizedIfPasswordIncorrect() {
    // given
    final String password = "1234";
    final User user = userBuilderWithDefaults().password(passwordEncoder.encode(password)).build();
    userRepository.save(user).block();
    final Login login =
        Login.builder().email(user.getEmail()).password("incorrectPassword").build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/login")
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    assertEquals("Invalid credentials", errorResp.get("message"));
  }

  @Test
  public void testLoginReturnsUnauthorizedIfOauthUserUsesPassword() {
    // given
    final User user =
        userBuilderWithDefaults()
            .oAuth2Provider(OAuth2Provider.GOOGLE)
            .password("placeholder")
            .build();
    userRepository.save(user).block();
    final Login login = Login.builder().email(user.getEmail()).password("mypass123").build();

    // when
    final Map<String, String> errorResp =
        webTestClient
            .post()
            .uri("/login")
            .bodyValue(login)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
            .returnResult()
            .getResponseBody();

    // then
    assertEquals("Invalid credentials", errorResp.get("message"));
  }
}
