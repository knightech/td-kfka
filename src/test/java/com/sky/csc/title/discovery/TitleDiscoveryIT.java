package com.sky.csc.title.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.csc.title.discovery.service.TitleDiscoveryService;
import com.sky.csc.title.discovery.util.JsonUtils;
import io.prometheus.client.CollectorRegistry;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TitleDiscoveryTestConfig.class)
public class TitleDiscoveryIT {

    private JsonUtils jsonUtils = new JsonUtils(new ObjectMapper());

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    TitleDiscoveryService titleDiscoveryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    static {
        //HACK Avoids duplicate metrics registration in case of Spring Boot dev-tools restarts
        CollectorRegistry.defaultRegistry.clear();
    }

    @Ignore
    @Test
    public void getItemsByGenreActionReturnsListOfGenres() throws IOException {

        List<JsonNode> titleList = objectMapper.readValue(
                jsonUtils.getJsonContent("expected/titles.json").toString(),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, JsonNode.class));

        // given
        given(titleDiscoveryService.getItemByGenre(anyString())).willReturn(titleList);

        //act
        ResponseEntity<List<JsonNode>> itemList = restTemplate.exchange(
                "/title-discovery/titles?genre=BollyWood",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<JsonNode>>(){});

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itemList.getBody().size()).isEqualTo(3);

    }

    @Ignore
    @Test
    public void getItemsByNonExistingGenreReturnsEmptyList() {

        //act
        ResponseEntity<List<JsonNode>> itemList = restTemplate.exchange(
                "/title-discovery/titles?genre=Romance",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<JsonNode>>(){});

        //assert
        assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itemList.getBody()).isEmpty();

    }


}
