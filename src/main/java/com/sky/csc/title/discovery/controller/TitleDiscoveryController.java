package com.sky.csc.title.discovery.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sky.csc.title.discovery.service.TitleDiscoveryService;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
//@AllArgsConstructor
public class TitleDiscoveryController {

    private final TitleDiscoveryService titleDiscoveryService;

    public TitleDiscoveryController(TitleDiscoveryService titleDiscoveryService) {
        this.titleDiscoveryService = titleDiscoveryService;
    }

    @GetMapping(value = "/title-discovery/titles", produces = APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity<List<JsonNode>> getItemsByGenre(
            @ApiParam(value = "The value for the genre to filter by")
            @RequestParam(value = "genre", required = false) String genre) {

        if (Objects.isNull(genre)) {
            throw new UnsupportedOperationException("Genre is required parameter for mvp1");
        }


        return new HttpEntity<>(titleDiscoveryService.getItemByGenre(genre));

    }

    @ApiIgnore
    @GetMapping(value = "/title-discovery/titles-stub", produces = APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity<List<JsonNode>> getItemsByGenreStub(@RequestParam(value = "genre", required = false) String genre) {

        if (Objects.isNull(genre)) {
            throw new UnsupportedOperationException("Genre is required parameter for mvp1");
        }


        return new HttpEntity<>(titleDiscoveryService.getItemByGenre(genre));

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private void genreRequiredHandler(UnsupportedOperationException ex) {
    }
}
