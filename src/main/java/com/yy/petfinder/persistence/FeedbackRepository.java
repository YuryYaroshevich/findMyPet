package com.yy.petfinder.persistence;

import com.yy.petfinder.model.Feedback;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, String> {}
