package com.yy.petfinder.util;

import com.yy.petfinder.rest.model.Identifiable;
import com.yy.petfinder.rest.model.Paging;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;

public final class PaginatedResponseHelper {
  public static final String NEXT_PAGE_TOKEN = "Next-page-token";

  public static <T extends Identifiable> ResponseEntity<List<T>> createResponse(
      final List<T> entities, final Paging paging) {
    final ResponseEntity.BodyBuilder respBuilder = ResponseEntity.ok();
    getNextPageToken(entities, paging.getPageSize())
        .ifPresent(nextPageToken -> respBuilder.header(NEXT_PAGE_TOKEN, nextPageToken));
    return respBuilder.body(entities);
  }

  private static <T extends Identifiable> Optional<String> getNextPageToken(
      final List<T> entities, final int pageSize) {
    if (entities.size() == pageSize) {
      final int lastPetAdIndex = entities.size() - 1;
      return Optional.of(entities.get(lastPetAdIndex).getId());
    } else {
      return Optional.empty();
    }
  }
}
