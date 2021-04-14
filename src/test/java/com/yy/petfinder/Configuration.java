package com.yy.petfinder;

import org.springframework.context.annotation.Import;

@org.springframework.context.annotation.Configuration
@Import(PetfinderApplication.class)
public class Configuration {
  static {
    System.setProperty("os.arch", "i686_64");
  }
}
