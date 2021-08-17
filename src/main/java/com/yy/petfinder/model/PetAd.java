package com.yy.petfinder.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@Document
public class PetAd {
  @Id @NonNull private String id;

  @NonNull private PetType petType;
  private List<String> colors;
  private String breed;
  @NonNull private String name;
  @NonNull private List<String> photoUrls;

  @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
  @NonNull
  private SearchArea searchArea;

  @Indexed @NonNull private String ownerId;

  @NonNull private Instant createdAt;
}
