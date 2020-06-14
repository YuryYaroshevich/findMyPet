package com.yy.petfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@SpringBootApplication
@EnableWebFluxSecurity
public class PetfinderApplication {

  public static void main(String[] args) {
    SpringApplication.run(PetfinderApplication.class, args);
  }
}
