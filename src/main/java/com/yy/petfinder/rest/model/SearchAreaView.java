package com.yy.petfinder.rest.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@NoArgsConstructor
@AllArgsConstructor
public class SearchAreaView {
  @NonNull
  @Getter
  private List<double[]> coordinates;
}
