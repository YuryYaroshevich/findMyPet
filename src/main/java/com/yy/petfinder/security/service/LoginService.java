package com.yy.petfinder.security.service;

import static com.yy.petfinder.exception.InvalidCredentialsException.invalidCredentials;

import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.rest.model.Login;
import com.yy.petfinder.security.model.JWTToken;
import com.yy.petfinder.security.model.LoginKey;
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
  private final UserRandomKeyRepository userRandomKeyRepository;

  public LoginService(
      final PasswordEncoder passwordEncoder,
      final UserDetailsService userDetailsService,
      final TokenService tokenService,
      final UserRandomKeyRepository userRandomKeyRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
    this.tokenService = tokenService;
    this.userRandomKeyRepository = userRandomKeyRepository;
  }

  public Mono<JWTToken> authenticate(final Login login) {
    final Mono<UserDetails> authenticatedUser =
        userDetailsService
            .findByEmail(login.getEmail())
            .filter(user -> passwordEncoder.matches(login.getPassword(), user.getPassword()))
            .switchIfEmpty(Mono.error(invalidCredentials()));
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

  public Mono<JWTToken> authenticate(final LoginKey loginKey) {
    return null;
  }
}
