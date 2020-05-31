package com.yy.petfinder.service;

import static com.yy.petfinder.testfactory.PetAdFactory.petAdBuilderWithDefaults;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.yy.petfinder.exception.OwnerIdUpdateException;
import com.yy.petfinder.model.PetAd;
import com.yy.petfinder.persistence.PetAdRepository;
import com.yy.petfinder.rest.model.PetAdView;
import com.yy.petfinder.rest.model.SearchAreaView;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class PetAdServiceTest {
  @InjectMocks private PetAdService petAdService;
  @Mock private PetAdRepository petAdRepository;

  @Test
  public void testUpdateAdThrowsExceptionIfOwnerUpdate() {
    // given
    final PetAd petAd = petAdBuilderWithDefaults().build();
    when(petAdRepository.findByUuid(eq(petAd.getUuid()))).thenReturn(Mono.just(petAd));

    final PetAdView updatedPetAd =
        PetAdView.builder()
            .id(petAd.getUuid())
            .searchArea(new SearchAreaView(petAd.getSearchArea().getCoordinatesList()))
            .petType(petAd.getPetType())
            .name(petAd.getName())
            .ownerId(UUID.randomUUID().toString())
            .imageBlob(petAd.getImageBlob())
            .colors(petAd.getColors())
            .build();

    // when then
    assertThrows(OwnerIdUpdateException.class, () -> petAdService.updateAd(id, updatedPetAd).block());
  }
}
