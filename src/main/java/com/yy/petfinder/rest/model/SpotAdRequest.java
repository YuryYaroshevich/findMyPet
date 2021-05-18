package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import lombok.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SpotAdRequest {
  private double longitude;
  private double latitude;
  private double radius;

  @NonNull private PetType petType;
}
