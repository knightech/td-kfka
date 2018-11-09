package com.sky.csc.title.discovery;

import com.sky.csc.title.discovery.model.ItemList;
import com.sky.csc.title.discovery.service.TitleDiscoveryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TitleDiscoveryIT {


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    TitleDiscoveryService titleDiscoveryService;


    @Test
    public void getItemsByGenreActionReturnsListOfGenresByAction() {

        //arrange
        ItemList test = titleDiscoveryService.getItemByGenre("Action");
        assertThat(test.getItemList().size()).isEqualTo(2);


        //act
        ResponseEntity<ItemList> itemList =
                restTemplate.getForEntity("/title-discovery/items?genre=Action",
                        ItemList.class);

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itemList.getBody().getItemList().size()).isEqualTo(2);

    }

    @Test
    public void getItemsByGenreComedyReturnsListOfGenresByComedy() {

        //act
        ResponseEntity<ItemList> itemList =
                restTemplate.getForEntity("/title-discovery/items?genre=Comedy",
                        ItemList.class);

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itemList.getBody().getItemList().size()).isEqualTo(2);

    }

    @Test
    public void getItemsByGenreBollyWoodReturnsListOfGenresByBollyWood() {

        //act
        ResponseEntity<ItemList> itemList =
                restTemplate.getForEntity("/title-discovery/items?genre=BollyWood",
                        ItemList.class);

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itemList.getBody().getItemList().size()).isEqualTo(1);

    }

    @Test
    public void getItemsByNonExistingGenreReturnsEmptyList() {


        //act
        ResponseEntity<ItemList> itemList =
                restTemplate.getForEntity("/title-discovery/items?genre=Romance",
                        ItemList.class);

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);


    }



}
