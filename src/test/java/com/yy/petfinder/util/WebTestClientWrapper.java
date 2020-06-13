package com.yy.petfinder.util;

import java.util.List;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public class WebTestClientWrapper {

  public static <T> List<T> getList(
      final WebTestClient webTestClient, final String uri, final Class<T> clazz) {
    return getExchange(webTestClient, uri, clazz).getResponseBody();
  }

  public static <T> EntityExchangeResult<List<T>> getExchange(
      final WebTestClient webTestClient, final String uri, final Class<T> clazz) {

    return webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(clazz)
        .returnResult();
  }

  public static <T> T get(
      final WebTestClient webTestClient, final String uri, final Class<T> clazz) {

    return webTestClient
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(clazz)
        .returnResult()
        .getResponseBody();
  }
}
