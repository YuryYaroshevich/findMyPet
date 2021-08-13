package com.yy.petfinder.util;

import com.yy.petfinder.model.PetAdState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPetAdStateConverter implements Converter<String, PetAdState> {
  @Override
  public PetAdState convert(final String value) {
    return PetAdState.of(value);
  }
}
