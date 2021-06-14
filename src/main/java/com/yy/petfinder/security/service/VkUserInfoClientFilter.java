package com.yy.petfinder.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.petfinder.exception.OAuth2FlowException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * Vk https://api.vk.com/method/users.get?v=5.89 response is: { "response": [ { "first_name": "Юра",
 * "id": 11583425, "last_name": "Ярошевич", "can_access_closed": true, "is_closed": false } ] } It
 * differs from Google and Facebook so we need to modify it to use common code base.
 */
@Component
public class VkUserInfoClientFilter {
  private final ObjectMapper objectMapper;

  @Autowired
  public VkUserInfoClientFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Mono<ClientResponse> unwrapResponseElement(final ClientResponse response) {
    return response
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .map(
            respBody -> {
              if (respBody.containsKey("response")) {
                return ((List) respBody.get("response")).get(0);
              }
              return respBody;
            })
        .map(
            data -> {
              try {
                return objectMapper.writeValueAsString(data);
              } catch (JsonProcessingException e) {
                throw new OAuth2FlowException();
              }
            })
        .map(data -> ClientResponse.from(response).body(data).build());
  }
}
