package com.yy.petfinder.security.service;

import com.yy.petfinder.rest.model.Login;
import com.yy.petfinder.security.model.JWTToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginService {
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final TokenService tokenService;

  public LoginService(
      final PasswordEncoder passwordEncoder,
      final UserDetailsService userDetailsService,
      final TokenService tokenService) {
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
    this.tokenService = tokenService;
  }

  public Mono<JWTToken> authenticate(final Login login) {
    final Mono<UserDetails> authenticatedUser =
        userDetailsService
            .findByUsername(login.getEmail())
            .filter(user -> passwordEncoder.matches(login.getPassword(), user.getPassword()))
            .switchIfEmpty(
                Mono.error(new BadCredentialsException("email or password is incorrect")));
    return authenticatedUser
        .doOnNext(
            userDetails ->
                ReactiveSecurityContextHolder.withAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities())))
        .map(UserDetails::getUsername)
        .map(tokenService::createToken)
        .map(JWTToken::new);
  }
}
