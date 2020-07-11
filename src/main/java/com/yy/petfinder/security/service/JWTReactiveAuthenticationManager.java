package com.yy.petfinder.security.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public class JWTReactiveAuthenticationManager implements ReactiveAuthenticationManager {
  private final ReactiveUserDetailsService userDetailsService;
  private final TokenService tokenService;

  public JWTReactiveAuthenticationManager(
      ReactiveUserDetailsService userDetailsService, TokenService tokenService) {
    this.userDetailsService = userDetailsService;
    this.tokenService = tokenService;
  }

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {
    return Mono.just(authentication)
        .cast(UsernamePasswordAuthenticationToken.class)
        .filter(auth -> tokenService.isValid((String) auth.getCredentials()))
        .flatMap(this::getUserDetails)
        .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Credentials")))
        .map(
            u ->
                new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    u.getAuthorities()));
  }

  private Mono<UserDetails> getUserDetails(
      final UsernamePasswordAuthenticationToken authentication) {
    final String username = authentication.getName();
    return userDetailsService
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Credentials")));
  }
}
