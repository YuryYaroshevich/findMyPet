package com.yy.petfinder.exception;

import java.util.Map;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;

@Component
public class ExceptionHandler extends DefaultErrorAttributes {
  private static final String STATUS_FIELD = "status";
  private static final String ERROR_FIELD = "error";

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
    final Throwable error = getError(request);
    final Map<String, Object> errorAttributes = super.getErrorAttributes(request, false);
    if (error instanceof BaseException) {
      final HttpStatus errorStatus = ((BaseException) error).getStatus();
      errorAttributes.replace(STATUS_FIELD, errorStatus.value());
      errorAttributes.replace(ERROR_FIELD, errorStatus.getReasonPhrase());
      return errorAttributes;
    } else if (error instanceof ServerWebInputException) {
      errorAttributes.replace(ERROR_FIELD, "Invalid json in request body");
    } else if (error instanceof Exception) {
      errorAttributes.replace(ERROR_FIELD, "Internal Server Error");
    }
    return errorAttributes;
  }
}
