package com.yy.petfinder.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.petfinder.exception.OAuth2FlowException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * VK oauth2 provider doesn't return token type and it breaks token parse workflow in spring
 * security codebase. So we need to set custom webClient which will add token_type using filter.
 */
@Component
public class OAuth2TokenTypeClientFilter {
  private final ObjectMapper objectMapper;

  @Autowired
  public OAuth2TokenTypeClientFilter(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Mono<ClientResponse> addTokenType(final ClientResponse response) {
    return response
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .map(
            fields -> {
              if (fields.containsKey("token_type")) {
                return fields;
              } else {
                final Map<String, Object> fieldsWithTokenType = new HashMap<>(fields);
                fieldsWithTokenType.put("token_type", "bearer");
                return fieldsWithTokenType;
              }
            })
        .map(
            fields -> {
              try {
                return objectMapper.writeValueAsString(fields);
              } catch (JsonProcessingException e) {
                throw new OAuth2FlowException();
              }
            })
        .map(token -> ClientResponse.from(response).body(token).build());
  }
}
