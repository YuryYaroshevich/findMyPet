package com.yy.petfinder.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
  private String nextPageToken = null;
  private int pageSize = 10;
}
