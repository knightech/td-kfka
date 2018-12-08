package com.sky.csc.title.discovery.util;

import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sky.csc.title.discovery.util.JsonUtils.Constants.TITLE_JSON_PROPERTY;

/**
 * Various common functions used to manage Json data in this application
 */
@Component
public class JsonUtils {

    public static class Constants {
        public static final String ITEM_ID_JSON_PROPERTY = "itemId";
        public static final String OFFER_ID_JSON_PROPERTY = "offerId";
        public static final String OFFERS_JSON_PROPERTY = "offers";
        public static final String TERMS_JSON_PROPERTY = "terms";
        public static final String TITLE_JSON_PROPERTY = "title";
        public static final String GENRE_JSON_PROPERY = "genre";

    }

    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads stub items from resources
     *
     * @param fileName the name of the file to load
     * @return the file's contents parsed as a JsonNode
     */
    public JsonNode getJsonContent(final String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        try {
            return objectMapper.readTree(classLoader.getResourceAsStream((fileName)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Extracts the supplied key value from the jsonString passed
     *
     * @param key        the key to lookup
     * @param jsonString the json object as String from where the key is to be obtained
     * @return the key's value as a String
     */
    public String getKeyFromJson(final String key, final String jsonString) {

        try {
            return objectMapper.readTree(jsonString).get(key).asText();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Strips whitespace and applies a regular expression to comma delimit objects and wrap them in an array
     *
     * @param rawConcatenatedInput the raw data in json format but without delimiters
     * @return array of objects comma delimited and wrapped in an array
     */
    public String formatToCSVArray(String rawConcatenatedInput) {
        String noNewLineCharacters = rawConcatenatedInput.replaceAll("\n+", "");
        String commaDelimitedJsonString = noNewLineCharacters.replaceAll("}\\s*\\{", "}\\,{");
        return String.format("[%s]", commaDelimitedJsonString);
    }

    /**
     * Returns a String representation of a parent node after adding the child array
     *
     * @param parentNodeAsString     the parent node
     * @param childNodeArrayAsString the child array
     * @return the parent combined with the child array as a String
     */
    public String combineParentWithChildrenNodes(String parentNodeAsString, String childNodeArrayAsString, String childArrayName) {

        if (Objects.isNull(parentNodeAsString)) {
            return StringUtil.EMPTY_STRING;

        }

        if (Objects.isNull(childNodeArrayAsString)) {
            return parentNodeAsString;
        }

        try {

            ObjectNode offerAsObjectNode = (ObjectNode) objectMapper.readTree(parentNodeAsString);

            JsonNode termsAsJsonNode = objectMapper.readTree(childNodeArrayAsString);

            offerAsObjectNode.set(childArrayName, termsAsJsonNode);

            return objectMapper.writeValueAsString(offerAsObjectNode);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Converts the Couchbase response to a JsonNode list
     *
     * @param result
     * @return
     */
    public List<JsonNode> extractJsonResult(N1qlQueryResult result) {
        return result.allRows().stream()
                .map(row -> {
                    try {
                        return objectMapper.readTree(row.value().toString()).get(TITLE_JSON_PROPERTY);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
