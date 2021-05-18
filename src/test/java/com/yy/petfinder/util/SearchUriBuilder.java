package com.yy.petfinder.util;

import com.yy.petfinder.rest.model.Paging;
import com.yy.petfinder.rest.model.PetSearchRequest;
import com.yy.petfinder.rest.model.SpotAdRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchUriBuilder {

  public static String searchUri(final PetSearchRequest petSearchReq) {
    return searchUriBuilderWithoutPagingBuilder(petSearchReq).build().toUriString();
  }

  public static String searchUri(final PetSearchRequest petSearchReq, final Paging paging) {
    final UriComponentsBuilder uriBuilder = searchUriBuilderWithoutPagingBuilder(petSearchReq);
    setPagingParams(paging, uriBuilder);

    return uriBuilder.build().toUriString();
  }

  private static void setPagingParams(Paging paging, UriComponentsBuilder uriBuilder) {
    if (paging.getNextPageToken() != null) {
      uriBuilder.queryParam("nextPageToken", paging.getNextPageToken());
    }
    uriBuilder.queryParam("pageSize", paging.getPageSize());
  }

  public static String getSpotAdsUri(final SpotAdRequest spotAdRequest, final Paging paging) {
    final UriComponentsBuilder uriBuilder = getSpotAdsUriWithoutPaging(spotAdRequest);
    setPagingParams(paging, uriBuilder);

    return uriBuilder.build().toUriString();
  }

  public static String getSpotAdsUri(final SpotAdRequest spotAdRequest) {
    return getSpotAdsUriWithoutPaging(spotAdRequest).build().toUriString();
  }

  private static UriComponentsBuilder getSpotAdsUriWithoutPaging(
      final SpotAdRequest spotAdRequest) {
    final UriComponentsBuilder uriBuilder =
        searchUriBuilderWithCoords(
            "/pets/spotAd/",
            spotAdRequest.getLongitude(),
            spotAdRequest.getLatitude(),
            spotAdRequest.getRadius());
    uriBuilder.queryParam("petType", spotAdRequest.getPetType());
    return uriBuilder;
  }

  private static UriComponentsBuilder searchUriBuilderWithoutPagingBuilder(
      final PetSearchRequest petSearchReq) {

    final UriComponentsBuilder uriBuilder =
        searchUriBuilderWithCoords(
            "/pets/ad/",
            petSearchReq.getLongitude(),
            petSearchReq.getLatitude(),
            petSearchReq.getRadius());
    if (petSearchReq.getPetType() != null) {
      uriBuilder.queryParam("petType", petSearchReq.getPetType().value());
    }
    if (petSearchReq.getColors() != null) {
      uriBuilder.queryParam("colors", petSearchReq.getColors());
    }
    if (petSearchReq.getBreed() != null) {
      uriBuilder.queryParam("breed", petSearchReq.getBreed());
    }
    return uriBuilder;
  }

  private static UriComponentsBuilder searchUriBuilderWithCoords(
      final String path, final double longitude, final double latitude, final double radius) {

    final UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(path)
            .queryParam("longitude", longitude)
            .queryParam("latitude", latitude)
            .queryParam("radius", radius);

    return uriBuilder;
  }
}
