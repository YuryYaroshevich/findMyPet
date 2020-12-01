package com.yy.petfinder.service;

import com.cloudinary.Cloudinary;
import com.yy.petfinder.exception.ImageOperationFailed;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ImageService {
  private static final Map<String, String> EMPTY_OPTIONS = Map.of();

  private Cloudinary cloudinary;

  @Autowired
  public ImageService(
      @Value("${cloudinary.cloudName}") final String cloudName,
      @Value("${cloudinary.apiKey}") final String apiKey,
      @Value("${cloudinary.apiSecret}") final String secretKey) {
    cloudinary =
        new Cloudinary(
            Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", secretKey));
  }

  public Mono<Void> deleteImages(final List<String> imageUrls) {
    if (imageUrls != null && !imageUrls.isEmpty()) {
      return Mono.fromRunnable(
          () -> {
            try {
              cloudinary.api().deleteResources(imageUrls, EMPTY_OPTIONS);
            } catch (Exception e) {
              throw new ImageOperationFailed(
                  "Failed to delete images.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
          });
    } else {
      return Mono.just(new Object()).then();
    }
  }
}
