package com.yy.petfinder.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class PetAdStatus {
  @NonNull private boolean found;
  private PetAdState state;
}
