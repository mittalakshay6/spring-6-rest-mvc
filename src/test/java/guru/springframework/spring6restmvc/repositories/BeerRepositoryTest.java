package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void saveBeerTest() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                        .beerName("My beer")
                        .beerStyle(BeerStyle.IPA)
                        .upc(UUID.randomUUID().toString())
                        .price(BigDecimal.TEN)
                .build());
        beerRepository.flush();
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

    @Test
    void saveBeerTestNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
             beerRepository.save(Beer.builder()
                    .beerName("My beer - more than 15 letters beer name")
                    .beerStyle(BeerStyle.IPA)
                    .upc(UUID.randomUUID().toString())
                    .price(BigDecimal.TEN)
                    .build());
            beerRepository.flush();
        });
    }
}
