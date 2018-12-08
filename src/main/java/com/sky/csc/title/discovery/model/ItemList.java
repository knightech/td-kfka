package com.sky.csc.title.discovery.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class ItemList {

    private List<JsonNode> itemList;

    public ItemList(List<JsonNode> itemList) {
        this.itemList = itemList;
    }

    public ItemList() {
        itemList = new ArrayList<>();
    }

    public void add(JsonNode item) {
        itemList.add(item);
    }

    public List<JsonNode> getItemList() {
        return itemList;
    }

    public void setItemList(List<JsonNode> itemList){
        this.itemList = itemList;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ItemList itemList1 = (ItemList) o;

        return new EqualsBuilder()
                .append(itemList, itemList1.itemList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(itemList)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ItemList{" +
                "itemList=" + itemList +
                '}';
    }
}
