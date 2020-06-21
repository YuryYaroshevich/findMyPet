package com.yy.petfinder.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class JWTReactiveAuthenticationManager implements ReactiveAuthenticationManager {
  private final ReactiveUserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  public JWTReactiveAuthenticationManager(
      ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {
    if (authentication.isAuthenticated()) {
      return Mono.just(authentication);
    }

    final String token = authentication.getCredentials().toString();
    // TODO: check that token has not expired
    final String username = authentication.getName();
    return Mono.just(authentication)
        .cast(UsernamePasswordAuthenticationToken.class)
        .flatMap(this::authenticateToken)
        .filter(
            u -> passwordEncoder.matches((String) authentication.getCredentials(), u.getPassword()))
        .switchIfEmpty(Mono.defer(this::raiseBadCredentials))
        .map(
            u ->
                new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    u.getAuthorities()));
  }

  private <T> Mono<T> raiseBadCredentials() {
    return Mono.error(new BadCredentialsException("Invalid Credentials"));
  }

  private Mono<UserDetails> authenticateToken(
      final UsernamePasswordAuthenticationToken authentication) {
    final String username = authentication.getName();
    return userDetailsService
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Credentials")));
  }
}
