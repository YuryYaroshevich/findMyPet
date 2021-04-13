package com.yy.petfinder.service;

import com.yy.petfinder.model.Feedback;
import com.yy.petfinder.persistence.FeedbackRepository;
import com.yy.petfinder.rest.model.FeedbackView;
import java.time.Clock;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FeedbackService {
  private final FeedbackRepository feedbackRepository;
  private final Clock clock;

  public FeedbackService(final FeedbackRepository feedbackRepository, final Clock clock) {
    this.feedbackRepository = feedbackRepository;
    this.clock = clock;
  }

  public Mono<Feedback> createFeedback(final FeedbackView feedbackView, final String userId) {
    final Feedback feedback =
        Feedback.builder()
            .id(new ObjectId().toHexString())
            .userId(userId)
            .text(feedbackView.getText())
            .createdAt(Instant.now(clock))
            .build();
    return feedbackRepository.save(feedback);
  }
}
