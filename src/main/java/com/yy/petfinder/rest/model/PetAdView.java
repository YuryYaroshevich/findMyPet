package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PetAdView {
  private String id;
  @NonNull private PetType petType;
  private List<String> colors;
  private String breed;
  @NonNull private String name;
  @NonNull private List<String> photoUrls;
  private List<String> removedPhotoUrls;
  @NonNull private SearchAreaView searchArea;
  private boolean found;
}
