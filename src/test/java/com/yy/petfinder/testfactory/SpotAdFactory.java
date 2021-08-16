package com.yy.petfinder.testfactory;

import com.yy.petfinder.model.PetType;
import com.yy.petfinder.model.SpotAd;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

public final class SpotAdFactory {
  public static SpotAd.SpotAdBuilder spotAdBuilderWithDefaults() {
    final String id = new ObjectId().toHexString();
    return SpotAd.builder()
        .id(id)
        .phone("375298887766")
        .petType(PetType.DOG)
        .photoIds(List.of("photo1", "photo2"))
        .description("I've seen this dog near my house")
        .radius(40000)
        .point(List.of(27.417068481445312, 53.885826945065915))
        .createdAt(Instant.now());
  }
}
