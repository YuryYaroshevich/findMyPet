package com.yy.petfinder.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class PetAd {
  @Id
  private final ObjectId id;
  @Indexed(unique = true, background = true)
  private final String uuid;

  private final PetType petType;
  private final String color;
  private final String name;

  private final byte[] imageBlob;

  private final SearchArea searchArea;

  private final String ownerId;

  public PetAd(ObjectId id, String uuid, PetType petType, String color, String name, byte[] imageBlob, SearchArea searchArea, String ownerId) {
    this.id = id;
    this.uuid = uuid;
    this.petType = petType;
    this.color = color;
    this.name = name;
    this.imageBlob = imageBlob;
    this.searchArea = searchArea;
    this.ownerId = ownerId;
  }
}
