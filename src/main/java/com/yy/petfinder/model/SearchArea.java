package com.yy.petfinder.model;

import java.util.List;
import java.util.Objects;

public class SearchArea {
  private static final String LOCATION_TYPE = "Polygon";

  private final String type;
  private final List<double[]> coordinates;

  public SearchArea(List<double[]> coordinates) {
    this.type = LOCATION_TYPE;
    this.coordinates = coordinates;
  }

  public String getType() {
    return type;
  }

  public List<double[]> getCoordinates() {
    return coordinates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchArea that = (SearchArea) o;
    return type.equals(that.type) &&
      coordinates.equals(that.coordinates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, coordinates);
  }
}
