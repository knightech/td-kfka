package com.sky.csc.title.discovery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.csc.title.discovery.model.ItemList;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TitleDiscoveryService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ItemList itemList = new ItemList(new ArrayList<>());
    private Map<String, List<JsonNode>> genreMap;


    public TitleDiscoveryService() {

    }

    @PostConstruct
    public void init() {

        itemList.add(getJsonContent("item-1001.json"));
        itemList.add(getJsonContent("item-1002.json"));
        itemList.add(getJsonContent("item-1003.json"));
        itemList.add(getJsonContent("item-1004.json"));
        itemList.add(getJsonContent("item-1005.json"));

        genreMap = itemList
                .getItemList().stream()
                .collect(Collectors.groupingBy(jsonNode -> jsonNode.get("genre").textValue()));
    }

    public ItemList getItemByGenre(String genre) {
        return new ItemList(genreMap.getOrDefault(genre, new ArrayList<>()));
    }

    private JsonNode getJsonContent(final String name) {

        ClassLoader classLoader = getClass().getClassLoader();

        File file = new File(Objects.requireNonNull(classLoader.getResource(name)).getFile());

        try {
            return objectMapper.readTree(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
