package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PetAdView {
  @NonNull private String uuid;
  @NonNull private PetType petType;
  private List<String> colors;
  private String breed;
  @NonNull private String name;
  @NonNull private byte[] imageBlob;
  @NonNull private SearchAreaView searchArea;
  @NonNull private String ownerId;
}
