package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SpotAdRequest {
  private double longitude;
  private double latitude;
  private double radius;

  @NonNull private PetType petType;
}
