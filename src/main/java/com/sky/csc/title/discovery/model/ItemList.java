package com.sky.csc.title.discovery.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ItemList {

    private final List<JsonNode> itemList;

    public ItemList(List<JsonNode> itemList) {
        this.itemList = itemList;
    }

    public ItemList() {
        itemList = new ArrayList<>();
    }

    public void add(JsonNode item) {
        itemList.add(item);
    }



}
