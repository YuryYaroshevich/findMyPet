package com.yy.petfinder.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Arrays;
import java.util.Objects;

public class PetAd {
  @Id
  private final ObjectId id;
  @Indexed(unique = true, background = true)
  private final String uuid;

  private final PetType petType;
  private final String color;
  private final String name;

  private final byte[] imageBlob;

  private final double[] lossPoint;
  private final double searchRadius;

  private final String ownerId;

  public PetAd(ObjectId id, String uuid, PetType petType, String color, String name, byte[] imageBlob, double[] lossPoint, double searchRadius, String ownerId) {
    this.id = id;
    this.uuid = uuid;
    this.petType = petType;
    this.color = color;
    this.name = name;
    this.imageBlob = imageBlob;
    this.lossPoint = lossPoint;
    this.searchRadius = searchRadius;
    this.ownerId = ownerId;
  }

  public ObjectId getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  public PetType getPetType() {
    return petType;
  }

  public String getColor() {
    return color;
  }

  public String getName() {
    return name;
  }

  public byte[] getImageBlob() {
    return imageBlob;
  }

  public double[] getLossPoint() {
    return lossPoint;
  }

  public double getSearchRadius() {
    return searchRadius;
  }

  public String getOwnerId() {
    return ownerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PetAd petAd = (PetAd) o;
    return Double.compare(petAd.searchRadius, searchRadius) == 0 &&
      id.equals(petAd.id) &&
      uuid.equals(petAd.uuid) &&
      petType == petAd.petType &&
      Objects.equals(color, petAd.color) &&
      name.equals(petAd.name) &&
      Arrays.equals(imageBlob, petAd.imageBlob) &&
      Arrays.equals(lossPoint, petAd.lossPoint) &&
      ownerId.equals(petAd.ownerId);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, uuid, petType, color, name, searchRadius, ownerId);
    result = 31 * result + Arrays.hashCode(imageBlob);
    result = 31 * result + Arrays.hashCode(lossPoint);
    return result;
  }
}
