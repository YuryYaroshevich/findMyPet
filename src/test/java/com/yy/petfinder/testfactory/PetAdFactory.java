package com.yy.petfinder.testfactory;

import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.model.PetType;
import com.yy.petfinder.model.SearchArea;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;

public class PetAdFactory {
  public static PetAd.PetAdBuilder petAdBuilderWithDefaults() {
    final ObjectId objectId = new ObjectId();
    final String uuid = UUID.randomUUID().toString();
    final SearchArea searchArea =
        SearchArea.of(
            List.of(
                List.of(53.911665, 27.469369),
                List.of(53.911867, 27.491685),
                List.of(53.899226, 27.491856),
                List.of(53.897405, 27.461129)));
    final PetType petType = PetType.DOG;
    final String name = "Fido";
    final String ownerId = UUID.randomUUID().toString();
    final byte[] imageBlob = {1, 2, 3};
    final String color = "black";

    return PetAd.builder()
        .id(objectId)
        .uuid(uuid)
        .searchArea(searchArea)
        .petType(petType)
        .name(name)
        .ownerId(ownerId)
        .imageBlob(imageBlob)
        .color(color);
  }
}
