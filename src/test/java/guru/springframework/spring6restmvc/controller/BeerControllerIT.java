package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BeerControllerIT {
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerController beerController;
    @Autowired
    BeerMapper beerMapper;

    @Test
    void testListBeer() {
        List<BeerDTO> beerDTOS = beerController.listBeers();
        assertThat(beerDTOS.size()).isEqualTo(3);
    }
    @Test
    @Transactional
    @Rollback
    void testEmptyListOfBeers() {
        beerRepository.deleteAll();
        List<BeerDTO> beerDTOS = beerController.listBeers();
        assertThat(beerDTOS.size()).isEqualTo(0);
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerController.getBeerById(beer.getId());
        assertThat(beerDTO).isNotNull();
    }

    @Test
    void testBeerIdNotFound() {
        UUID beerId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> beerController.getBeerById(beerId));

    }

    @Test
    @Transactional
    @Rollback
    void testSaveNewBeer() {
        BeerDTO newTestBeer = BeerDTO.builder().beerName("New Test Beer").build();
        ResponseEntity<?> newBeerResponse = beerController.handlePost(newTestBeer);
        assertThat(newBeerResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(newBeerResponse.getHeaders().getLocation()).isNotNull();
        String[] locationUUID = newBeerResponse.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);
        assertThat(beerRepository.findById(savedUUID).orElse(null)).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateExistingBeer() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        beerDTO.setBeerName("UPDATED");
        ResponseEntity<?> responseEntity = beerController.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        Beer updatedBeer = beerRepository.findById(beer.getId()).orElse(null);
        assertThat(updatedBeer).isNotNull();
        assertThat(updatedBeer.getBeerName()).isEqualTo("UPDATED");
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build()));
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteBeer() {
        Beer beer = beerRepository.findAll().get(0);
        ResponseEntity<?> responseEntity = beerController.deleteById(beer.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value()));
        assertThat(beerRepository.findById(beer.getId())).isEmpty();
    }

    @Test
    void testDeleteBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> beerController.deleteById(UUID.randomUUID()));
    }
}
