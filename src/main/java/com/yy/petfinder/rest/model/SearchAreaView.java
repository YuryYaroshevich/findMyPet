package com.yy.petfinder.rest.model;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class SearchAreaView {
  @NonNull private List<List<Double>> coordinates;
}
