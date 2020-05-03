package com.yy.petfinder.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@Document
public class PetAd {
  @Id @NonNull private ObjectId id;

  @Indexed(unique = true, background = true)
  @NonNull
  private String uuid;

  @NonNull private PetType petType;
  private String color;
  @NonNull private String name;
  @NonNull private byte[] imageBlob;

  @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
  @NonNull
  private SearchArea searchArea;

  @NonNull private String ownerId;
}
