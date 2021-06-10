package com.yy.petfinder.security;

import static com.yy.petfinder.util.PaginatedResponseHelper.NEXT_PAGE_TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.petfinder.security.model.OAuthTokenWrapper;
import com.yy.petfinder.security.service.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class SecurityConfiguration {
  private final UserDetailsService reactiveUserDetailsService;
  private final TokenService tokenService;
  private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler authenticationFailureHandler;
  private final ObjectMapper objectMapper;

  public SecurityConfiguration(
      UserDetailsService reactiveUserDetailsService,
      TokenService tokenProvider,
      OAuth2AuthenticationSuccessHandler authenticationSuccessHandler,
      OAuth2AuthenticationFailureHandler authenticationFailureHandler,
      ObjectMapper objectMapper) {
    this.reactiveUserDetailsService = reactiveUserDetailsService;
    this.tokenService = tokenProvider;
    this.authenticationSuccessHandler = authenticationSuccessHandler;
    this.authenticationFailureHandler = authenticationFailureHandler;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

    http.httpBasic().disable().formLogin().disable().csrf().disable().logout().disable();

    http.authorizeExchange()
        .pathMatchers(
            "/login",
            "/login-with-key",
            "/signUp",
            "/users/newPasswordEmail",
            "/users/newPassword",
            "/anonymous-feedback")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/users/**/public")
        .permitAll()
        .matchers(new PetSearchRequestMatcher(), new SpotAdRequestMatcher())
        .permitAll()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .and()
        .addFilterAt(webFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
        .authorizeExchange()
        .anyExchange()
        .authenticated()
        .and()
        .exceptionHandling(
            exceptionHandlingSpec ->
                exceptionHandlingSpec.authenticationEntryPoint(
                    new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
        .oauth2Login()
        .authenticationSuccessHandler(authenticationSuccessHandler)
        .authenticationFailureHandler(authenticationFailureHandler);

    return http.build();
  }

  @Bean
  public AuthenticationWebFilter webFilter() {
    AuthenticationWebFilter authenticationWebFilter =
        new AuthenticationWebFilter(repositoryReactiveAuthenticationManager());
    authenticationWebFilter.setServerAuthenticationConverter(
        new TokenAuthenticationConverter(tokenService));
    return authenticationWebFilter;
  }

  @Bean
  public JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager() {
    JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager =
        new JWTReactiveAuthenticationManager(reactiveUserDetailsService, tokenService);
    return repositoryReactiveAuthenticationManager;
  }

  @Bean
  public CorsWebFilter corsWebFilter(@Value("${allowed.origin}") final String allowedOrigin) {
    final CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(List.of(allowedOrigin));
    corsConfig.setMaxAge(8000L);
    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT"));
    corsConfig.setAllowedHeaders(List.of("Content-Type", "Authorization"));
    corsConfig.setExposedHeaders(List.of(NEXT_PAGE_TOKEN));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  @Bean
  public Pbkdf2PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder();
  }

  /**
   * VK oauth2 provider doesn't return token type and it breaks token parse workflow in spring
   * security codebase. So we need to set custom webClient which will add token_type using filter.
   */
  @Bean
  public WebClientReactiveAuthorizationCodeTokenResponseClient
      webClientReactiveAuthorizationCodeTokenResponseClient() {
    final WebClientReactiveAuthorizationCodeTokenResponseClient client =
        new WebClientReactiveAuthorizationCodeTokenResponseClient();
    client.setWebClient(
        WebClient.builder()
            .filter(
                ExchangeFilterFunction.ofResponseProcessor(
                    resp ->
                        resp.bodyToMono(OAuthTokenWrapper.class)
                            .map(token -> token.getFields())
                            .map(
                                fields -> {
                                  if (fields.containsKey("token_type")) {
                                    return fields;
                                  } else {
                                    final Map<String, Object> fieldsWithTokenType =
                                        new HashMap<>(fields);
                                    fieldsWithTokenType.put("token_type", "bearer");
                                    return fieldsWithTokenType;
                                  }
                                })
                            .map(fields -> new OAuthTokenWrapper(fields))
                            .map(
                                token -> {
                                  try {
                                    return objectMapper.writeValueAsString(token);
                                  } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                  }
                                })
                            .map(token -> ClientResponse.from(resp).body(token).build())))
            .build());
    return client;
  }
}
