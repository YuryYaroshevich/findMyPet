package com.yy.petfinder.rest.model;

import static com.yy.petfinder.rest.PetAdController.DEFAULT_PAGE_SIZE;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
  private String nextPageToken = null;
  private int pageSize = DEFAULT_PAGE_SIZE;
}
