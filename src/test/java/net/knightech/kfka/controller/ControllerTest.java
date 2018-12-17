package net.knightech.kfka.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.knightech.kfka.service.CouchbaseService;
import net.knightech.kfka.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(Controller.class)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

private ObjectMapper objectMapper = new ObjectMapper();
    private JsonUtils jsonUtils = new JsonUtils(objectMapper);

    @MockBean
    private CouchbaseService couchbaseService;

    @Test
    public void getItemsByGenreList() throws Exception {

        List<JsonNode> titleList = objectMapper.readValue(
                jsonUtils.getJsonContent("expected/titles.json").toString(),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class, JsonNode.class));

        given(couchbaseService.getItemByGenre(anyString())).willReturn(titleList);

        mockMvc.perform(MockMvcRequestBuilders.get("/title-discovery/titles?genre=Comedy"))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$.[0].itemId", is("09ecfa56-7902-4dfb-82b1-cd24cf4a17ed")))
                .andExpect(jsonPath("$.[0].item-type", is("Programme")))
                .andExpect(jsonPath("$.[0].sub-type", is("Special")));

        verify(couchbaseService).getItemByGenre(anyString());

        verifyNoMoreInteractions(couchbaseService);

    }

}