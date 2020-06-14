package com.yy.petfinder;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public static Pbkdf2PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder();
  }

  @Bean
  public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
    return http.exceptionHandling()
        .authenticationEntryPoint(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    }))
        .accessDeniedHandler(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    }))
        .and()
        .csrf()
        .disable()
        .formLogin()
        .disable()
        .httpBasic()
        .disable()
        // .authenticationManager(authenticationManager)
        // .securityContextRepository(securityContextRepository)
        .authorizeExchange()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .pathMatchers("/login")
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .build();
  }
}
