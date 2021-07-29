package com.yy.petfinder.security.service;

import static com.yy.petfinder.exception.InvalidCredentialsException.invalidCredentials;
import static java.util.function.Predicate.not;

import com.yy.petfinder.exception.OAuth2FlowException;
import com.yy.petfinder.model.User;
import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.rest.model.Login;
import com.yy.petfinder.security.model.JWTToken;
import com.yy.petfinder.security.model.LoginKey;
import com.yy.petfinder.service.UserService;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginService {
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final UserService userService;
  private final UserRandomKeyRepository userRandomKeyRepository;

  public LoginService(
      final PasswordEncoder passwordEncoder,
      final TokenService tokenService,
      final UserService userService,
      final UserRandomKeyRepository userRandomKeyRepository) {
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.userService = userService;
    this.userRandomKeyRepository = userRandomKeyRepository;
  }

  public Mono<JWTToken> authenticate(final Login login) {
    final Mono<User> authenticatedUser =
        userService
            .getUserByEmail(login.getEmail())
            .filter(not(User::isOAuth2Authenticated))
            .filter(user -> passwordEncoder.matches(login.getPassword(), user.getPassword()))
            .switchIfEmpty(Mono.error(invalidCredentials()));
    return authenticatedUser
        .doOnNext(
            user ->
                ReactiveSecurityContextHolder.withAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        user.getId(), user.getPassword(), List.of())))
        .map(User::getId)
        .map(tokenService::createToken)
        .map(JWTToken::new);
  }

  public Mono<JWTToken> authenticate(final LoginKey loginKey) {
    return userRandomKeyRepository
        .findAndRemove(loginKey.getId(), loginKey.getKey())
        .switchIfEmpty(Mono.error(new OAuth2FlowException()))
        .map(UserRandomKey::getId)
        .map(tokenService::createToken)
        .map(JWTToken::new);
  }
}
