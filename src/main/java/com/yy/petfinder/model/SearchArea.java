package com.yy.petfinder.model;

import java.util.List;
import lombok.Value;

@Value
public class SearchArea {
  private static final String LOCATION_TYPE = "Polygon";

  private String type;
  private List<double[]> coordinates;

  public static SearchArea of(final List<double[]> coordinates) {
    return new SearchArea(LOCATION_TYPE, coordinates);
  }
}
