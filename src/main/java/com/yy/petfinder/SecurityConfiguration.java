package com.yy.petfinder;

import com.yy.petfinder.service.JWTHeadersExchangeMatcher;
import com.yy.petfinder.service.JWTReactiveAuthenticationManager;
import com.yy.petfinder.service.ReactiveUserDetailsServiceImpl;
import com.yy.petfinder.service.TokenAuthenticationConverter;
import com.yy.petfinder.service.TokenProvider;
import com.yy.petfinder.service.UnauthorizedAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class SecurityConfiguration {
  private final ReactiveUserDetailsServiceImpl reactiveUserDetailsService;
  private final TokenProvider tokenProvider;

  public SecurityConfiguration(
      ReactiveUserDetailsServiceImpl reactiveUserDetailsService, TokenProvider tokenProvider) {
    this.reactiveUserDetailsService = reactiveUserDetailsService;
    this.tokenProvider = tokenProvider;
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http, UnauthorizedAuthenticationEntryPoint entryPoint) {

    http.httpBasic().disable().formLogin().disable().csrf().disable().logout().disable();

    http.exceptionHandling()
        .authenticationEntryPoint(entryPoint)
        .and()
        .authorizeExchange()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .and()
        .addFilterAt(webFilter(), SecurityWebFiltersOrder.AUTHORIZATION);

    return http.build();
  }

  @Bean
  public AuthenticationWebFilter webFilter() {
    AuthenticationWebFilter authenticationWebFilter =
        new AuthenticationWebFilter(repositoryReactiveAuthenticationManager());
    authenticationWebFilter.setServerAuthenticationConverter(
        new TokenAuthenticationConverter(tokenProvider));
    authenticationWebFilter.setRequiresAuthenticationMatcher(new JWTHeadersExchangeMatcher());
    authenticationWebFilter.setSecurityContextRepository(
        new WebSessionServerSecurityContextRepository());
    return authenticationWebFilter;
  }

  @Bean
  public JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager() {
    JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager =
        new JWTReactiveAuthenticationManager(reactiveUserDetailsService, passwordEncoder());
    return repositoryReactiveAuthenticationManager;
  }

  @Bean
  public static Pbkdf2PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder();
  }
}
