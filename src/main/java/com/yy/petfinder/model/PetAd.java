package com.yy.petfinder.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Value
@Builder
public class PetAd {
  @Id @NonNull private ObjectId id;

  @Indexed(unique = true, background = true)
  @NonNull
  private String uuid;

  @NonNull private PetType petType;
  private String color;
  @NonNull private String name;
  @NonNull private byte[] imageBlob;
  @NonNull private SearchArea searchArea;
  @NonNull private String ownerId;
}
