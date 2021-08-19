package com.yy.petfinder.util;

import com.yy.petfinder.model.PetAdResult;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPetAdStateConverter implements Converter<String, PetAdResult> {
  @Override
  public PetAdResult convert(final String value) {
    return PetAdResult.of(value);
  }
}
