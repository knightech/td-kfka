package net.knightech.kfka.service;

import com.fasterxml.jackson.databind.JsonNode;
import net.knightech.kfka.repository.CouchbaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouchbaseService {

    private final CouchbaseRepository couchbaseRepository;

    public CouchbaseService(CouchbaseRepository couchbaseRepository) {
        this.couchbaseRepository = couchbaseRepository;
    }

    public List<JsonNode> getItemByGenre(String genre) {

        return couchbaseRepository.getItemByGenre(genre);
    }

}
