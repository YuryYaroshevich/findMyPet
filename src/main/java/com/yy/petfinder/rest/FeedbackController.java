package com.yy.petfinder.rest;

import static com.yy.petfinder.util.UserIdRetriever.userIdFromContext;

import com.yy.petfinder.rest.model.FeedbackView;
import com.yy.petfinder.service.FeedbackService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FeedbackController {
  private static final String ANONYMOUS_USER = "anonymous_user";

  private final FeedbackService feedbackService;

  public FeedbackController(final FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/feedback")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> createFeedback(@RequestBody @Valid final FeedbackView feedback) {
    return userIdFromContext()
        .flatMap(userId -> feedbackService.createFeedback(feedback, userId))
        .then();
  }

  @PostMapping("/anonymous-feedback")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> createAnonymousFeedback(@RequestBody @Valid final FeedbackView feedback) {
    return feedbackService.createFeedback(feedback, ANONYMOUS_USER).then();
  }
}
