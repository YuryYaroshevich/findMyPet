package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PetSearchRequest {
  private double longitude;
  private double latitude;
  private double radius;
  private PetType petType;
}
