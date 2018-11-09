package com.sky.csc.title.discovery.controller;

import com.sky.csc.title.discovery.model.ItemList;
import com.sky.csc.title.discovery.service.TitleDiscoveryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@AllArgsConstructor
public class TitleDiscoveryController  {

    private final TitleDiscoveryService titleDiscoveryService;

    @GetMapping("/title-discovery/items")
    public HttpEntity<ItemList> getItemsByGenre(@RequestParam(value = "genre", required = false) String genre){

        if(Objects.isNull(genre)){
            throw new UnsupportedOperationException("Genre is required parameter for mvp1");
        }


        return new HttpEntity<>(titleDiscoveryService.getItemByGenre(genre));

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private void genreRequiredHandler(UnsupportedOperationException ex){}
}
