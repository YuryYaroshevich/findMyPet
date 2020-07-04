package com.yy.petfinder.security;

import com.yy.petfinder.security.service.JWTHeadersExchangeMatcher;
import com.yy.petfinder.security.service.JWTReactiveAuthenticationManager;
import com.yy.petfinder.security.service.PetSearchRequestMatcher;
import com.yy.petfinder.security.service.TokenAuthenticationConverter;
import com.yy.petfinder.security.service.TokenService;
import com.yy.petfinder.security.service.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class SecurityConfiguration {
  private final UserDetailsService reactiveUserDetailsService;
  private final TokenService tokenService;

  public SecurityConfiguration(
      UserDetailsService reactiveUserDetailsService, TokenService tokenProvider) {
    this.reactiveUserDetailsService = reactiveUserDetailsService;
    this.tokenService = tokenProvider;
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

    http.httpBasic().disable().formLogin().disable().csrf().disable().logout().disable();

    http.authorizeExchange()
        .pathMatchers("/login", "/signUp")
        .permitAll()
        .matchers(new PetSearchRequestMatcher())
        .permitAll()
        .and()
        .addFilterAt(webFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
        .authorizeExchange()
        .anyExchange()
        .authenticated();

    return http.build();
  }

  @Bean
  public AuthenticationWebFilter webFilter() {
    AuthenticationWebFilter authenticationWebFilter =
        new AuthenticationWebFilter(repositoryReactiveAuthenticationManager());
    authenticationWebFilter.setServerAuthenticationConverter(
        new TokenAuthenticationConverter(tokenService));
    authenticationWebFilter.setRequiresAuthenticationMatcher(new JWTHeadersExchangeMatcher());
    return authenticationWebFilter;
  }

  @Bean
  public JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager() {
    JWTReactiveAuthenticationManager repositoryReactiveAuthenticationManager =
        new JWTReactiveAuthenticationManager(reactiveUserDetailsService, tokenService);
    return repositoryReactiveAuthenticationManager;
  }

  @Bean
  public Pbkdf2PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder();
  }
}
