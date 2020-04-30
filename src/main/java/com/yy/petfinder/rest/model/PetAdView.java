package com.yy.petfinder.rest.model;

import com.yy.petfinder.model.PetType;
import com.yy.petfinder.model.SearchArea;

public class PetAdView {
  private final String uuid;

  private final PetType petType;
  private final String color;
  private final String name;

  private final byte[] imageBlob;

  private final SearchArea searchArea;

  private final String ownerId;

  public PetAdView(String uuid, PetType petType, String color, String name, byte[] imageBlob, SearchArea searchArea, String ownerId) {
    this.uuid = uuid;
    this.petType = petType;
    this.color = color;
    this.name = name;
    this.imageBlob = imageBlob;
    this.searchArea = searchArea;
    this.ownerId = ownerId;
  }
}
