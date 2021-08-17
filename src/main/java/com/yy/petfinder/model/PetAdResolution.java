package com.yy.petfinder.model;

import java.time.Instant;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@Document
public class PetAdResolution {
  @Id @NonNull private String id;
  @NonNull private PetAdState petAdState;
  @NonNull private PetType petType;
  private String breed;

  @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
  @NonNull
  private SearchArea searchArea;

  @NonNull private Instant createdAt;
}
