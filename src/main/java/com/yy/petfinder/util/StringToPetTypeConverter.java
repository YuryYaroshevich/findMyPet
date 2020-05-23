package com.yy.petfinder.util;

import com.yy.petfinder.model.PetType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPetTypeConverter implements Converter<String, PetType> {
  @Override
  public PetType convert(String source) {
    return PetType.of(source);
  }
}
