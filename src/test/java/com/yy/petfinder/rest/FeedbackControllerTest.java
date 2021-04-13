package com.yy.petfinder.rest;

import com.yy.petfinder.model.Feedback;
import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.FeedbackRepository;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.FeedbackView;
import com.yy.petfinder.security.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.yy.petfinder.testfactory.UserFactory.userBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FeedbackControllerTest {
  @Autowired
  private WebTestClient webTestClient;

  @Autowired private FeedbackRepository feedbackRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private TokenService tokenService;

  private String authHeaderValue;
  private String userId;

  @BeforeEach
  public void setup() {
    feedbackRepository.deleteAll().block();
    userRepository.deleteAll().block();
    final User user = userBuilderWithDefaults().build();
    userRepository.save(user).block();
    authHeaderValue = "Bearer " + tokenService.createToken(user.getId());
    userId = user.getId();
  }

  @Test
  public void testPostFeedbackStoresFeedback() {
    final FeedbackView feedbackView = new FeedbackView("Very helpful app!!!");

    webTestClient
      .post()
      .uri("/feedback")
      .header(AUTHORIZATION, authHeaderValue)
      .bodyValue(feedbackView)
      .exchange()
      .expectStatus()
      .isCreated();

    final List<Feedback> feedbackList = feedbackRepository
      .findAll().collectList().block();

    assertEquals(1, feedbackList.size());
    final Feedback feedback = feedbackList.get(0);
    assertEquals(userId, feedback.getUserId());
    assertEquals(feedbackView.getText(), feedback.getText());
  }

  @Test
  public void testPostAnonymousFeedbackStoresFeedback() {
    final FeedbackView feedbackView = new FeedbackView("Very helpful app!!!");

    webTestClient
      .post()
      .uri("/anonymous-feedback")
      .bodyValue(feedbackView)
      .exchange()
      .expectStatus()
      .isCreated();

    final List<Feedback> feedbackList = feedbackRepository
      .findAll().collectList().block();

    assertEquals(1, feedbackList.size());
    final Feedback feedback = feedbackList.get(0);
    assertEquals("anonymous_user", feedback.getUserId());
    assertEquals(feedbackView.getText(), feedback.getText());
  }
}
