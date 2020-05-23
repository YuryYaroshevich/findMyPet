package com.yy.petfinder.util;

import com.yy.petfinder.model.PetType;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchUriBuilder {
  public static String searchUri(
      final double longitude, final double latitude, final double radius, final PetType petType) {

    final UriComponentsBuilder uriBuilder = searchUriBuilder(longitude, latitude, radius);
    if (petType != null) {
      uriBuilder.queryParam("petType", petType.value());
    }
    return uriBuilder.build().toUriString();
  }

  public static String searchUri(
      final double longitude, final double latitude, final double radius) {

    final UriComponentsBuilder uriBuilder = searchUriBuilder(longitude, latitude, radius);

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
