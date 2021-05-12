package com.yy.petfinder.model;

import java.util.List;
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
public class SpotAd {
  @Id @NonNull private String id;
  @NonNull private PetType petType;
  private String description;
  private String phone;
  @NonNull private List<String> photoIds;

  @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
  @NonNull
  private List<Double> point;

  private double radius;
}
