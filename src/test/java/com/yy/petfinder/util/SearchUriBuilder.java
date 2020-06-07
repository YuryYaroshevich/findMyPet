package com.yy.petfinder.util;

import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetSearchRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchUriBuilder {

  public static String searchUri(final PetSearchRequest petSearchReq) {
    final UriComponentsBuilder uriBuilder =
        searchUriBuilder(
            petSearchReq.getLongitude(), petSearchReq.getLatitude(), petSearchReq.getRadius());
    if (petSearchReq.getPetType() != null) {
      uriBuilder.queryParam("petType", petSearchReq.getPetType().value());
    }
    if (petSearchReq.getColors() != null) {
      uriBuilder.queryParam("colors", petSearchReq.getColors());
    }
    if (petSearchReq.getBreed() != null) {
      uriBuilder.queryParam("breed", petSearchReq.getBreed());
    }
    return uriBuilder.build().toUriString();
  }

  public static String searchUri(final PetSearchRequest petSearchReq, final Paging paging) {
    final UriComponentsBuilder uriBuilder =
        searchUriBuilder(
            petSearchReq.getLongitude(), petSearchReq.getLatitude(), petSearchReq.getRadius());
    if (petSearchReq.getPetType() != null) {
      uriBuilder.queryParam("petType", petSearchReq.getPetType().value());
    }
    if (petSearchReq.getColors() != null) {
      uriBuilder.queryParam("colors", petSearchReq.getColors());
    }
    if (petSearchReq.getBreed() != null) {
      uriBuilder.queryParam("breed", petSearchReq.getBreed());
    }
    if (paging.getNextPageToken() != null) {
      uriBuilder.queryParam("nextPageToken", paging.getNextPageToken());
    }
    uriBuilder.queryParam("pageSize", paging.getPageSize());

    return uriBuilder.build().toUriString();
  }

  private static UriComponentsBuilder searchUriBuilder(
      final double longitude, final double latitude, final double radius) {

    final UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString("/pets/ad")
            .queryParam("longitude", longitude)
            .queryParam("latitude", latitude)
            .queryParam("radius", radius);

    return uriBuilder;
  }
}
