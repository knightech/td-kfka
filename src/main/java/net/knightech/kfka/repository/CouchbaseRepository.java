package net.knightech.kfka.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.ParameterizedN1qlQuery;
import com.fasterxml.jackson.databind.JsonNode;
import net.knightech.kfka.util.JsonUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CouchbaseRepository {

    private static final String N1QL_QUERY_TITLES_BY_GENRE = " SELECT * FROM `tds-data` AS title WHERE title.genre=$genre";
    private final JsonUtils jsonUtils;
    private final Bucket titleBucket;

    public CouchbaseRepository(JsonUtils jsonUtils, Bucket titleBucket) {
        this.jsonUtils = jsonUtils;
        this.titleBucket = titleBucket;
    }

    public List<JsonNode> getItemByGenre(String genre) {

        JsonObject placeholderValues = JsonObject.create().put(JsonUtils.Constants.GENRE_JSON_PROPERY, genre);

        ParameterizedN1qlQuery parameterizedGetByGenreQuery = N1qlQuery.parameterized(N1QL_QUERY_TITLES_BY_GENRE, placeholderValues);

        N1qlQueryResult query = titleBucket.query(parameterizedGetByGenreQuery);

        return jsonUtils.extractJsonResult(query);
    }
}
