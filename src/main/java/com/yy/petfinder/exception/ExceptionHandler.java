package com.yy.petfinder.exception;

import java.util.Map;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

@Component
public class ExceptionHandler extends DefaultErrorAttributes {
  private static final String STATUS_FIELD = "status";
  private static final String ERROR_FIELD = "error";
  private static final String MESSAGE_FIELD = "message";

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
    final Throwable error = getError(request);
    final Map<String, Object> errorAttributes = super.getErrorAttributes(request, false);
    if (error instanceof BaseException) {
      final var errorStatus = ((BaseException) error).getStatus();
      errorAttributes.replace(STATUS_FIELD, errorStatus.value());
      errorAttributes.replace(ERROR_FIELD, errorStatus.getReasonPhrase());
      return errorAttributes;
    } else if (error instanceof ServerWebInputException) {
      errorAttributes.replace(ERROR_FIELD, "Invalid json in request body");
    } else if (error instanceof OAuth2AuthorizationException) {
      errorAttributes.replace(ERROR_FIELD, "Failed to authorize");
      errorAttributes.replace(MESSAGE_FIELD, "Failed to authorize");
      errorAttributes.replace(STATUS_FIELD, HttpStatus.UNAUTHORIZED.value());
    } else if (error instanceof ResponseStatusException) {
      final ResponseStatusException responseStatusException = (ResponseStatusException) error;
      errorAttributes.replace(MESSAGE_FIELD, responseStatusException.getMessage());
      errorAttributes.replace(STATUS_FIELD, responseStatusException.getStatus().value());
    } else if (error instanceof Exception) {
      errorAttributes.replace(ERROR_FIELD, "Internal Server Error");
    }
    return errorAttributes;
  }
}
