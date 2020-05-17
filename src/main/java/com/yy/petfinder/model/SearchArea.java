package com.yy.petfinder.model;

import java.util.List;
import lombok.Value;

@Value
public class SearchArea {
  private static final String LOCATION_TYPE = "Polygon";

  private String type;
  private List<List<List<Double>>> coordinates;

  public static SearchArea of(final List<List<Double>> coordinates) {
    return new SearchArea(LOCATION_TYPE, List.of(coordinates));
  }

  public List<List<Double>> getCoordinatesList() {
    return coordinates.get(0);
  }
}
