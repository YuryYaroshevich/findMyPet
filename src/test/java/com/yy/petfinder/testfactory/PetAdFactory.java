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
                List.of(27.417068481445312, 53.885826945065915),
                List.of(27.420544624328613, 53.881248454798666),
                List.of(27.4273681640625, 53.884385154154224),
                List.of(27.425780296325684, 53.88805277023041),
                List.of(27.417068481445312, 53.885826945065915)));
    final PetType petType = PetType.DOG;
    final String name = "Fido";
    final String ownerId = UUID.randomUUID().toString();
    final byte[] imageBlob = {1, 2, 3};
    final List<String> colors = List.of("black");

    return PetAd.builder()
        .id(objectId)
        .uuid(uuid)
        .searchArea(searchArea)
        .petType(petType)
        .name(name)
        .ownerId(ownerId)
        .imageBlob(imageBlob)
        .colors(colors);
  }
}
