package com.sky.csc.title.discovery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sky.csc.title.discovery.repository.TitleDiscoveryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TitleDiscoveryService {

    private final TitleDiscoveryRepository titleDiscoveryRepository;

    public TitleDiscoveryService(TitleDiscoveryRepository titleDiscoveryRepository) {
        this.titleDiscoveryRepository = titleDiscoveryRepository;
    }

    public List<JsonNode> getItemByGenre(String genre) {

        return titleDiscoveryRepository.getItemByGenre(genre);
    }

}
