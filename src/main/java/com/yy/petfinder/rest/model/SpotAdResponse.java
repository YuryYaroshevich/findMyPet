package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SpotAdResponse implements Identifiable {
  @NonNull private String id;

  private double longitude;
  private double latitude;
  private double radius;

  @NonNull private PetType petType;
  private String description;
  private String phone;
  @NonNull private List<String> photoIds;
}
