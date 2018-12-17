package net.knightech.kfka.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.knightech.kfka.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class CouchbaseRepositoryTest {

    @Mock
    private Bucket titleBucket;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CouchbaseRepository couchbaseRepository;

    private JsonUtils jsonUtils = new JsonUtils(objectMapper);

    @Before
    public void setUp() {
        couchbaseRepository =
                new CouchbaseRepository(jsonUtils, titleBucket);
    }

    @Mock
    N1qlQueryResult n1qlQueryResult;

    @Test
    public void getItemByGenre() {

        assertThat(titleBucket).isNotNull();

        String expected = jsonUtils.getJsonContent("expected/title-couchbase.json").toString();

        N1qlQueryRow defaultAsyncN1qlQueryRow = new DefaultN1qlQueryRow(new DefaultAsyncN1qlQueryRow(expected.getBytes()));

        List<N1qlQueryRow> resultList = Collections.singletonList(defaultAsyncN1qlQueryRow);

        // arrange
        given(titleBucket.query(any(ParameterizedN1qlQuery.class))).willReturn(n1qlQueryResult);

        given(n1qlQueryResult.allRows()).willReturn(resultList);

        //act
        List<JsonNode> itemList = couchbaseRepository.getItemByGenre("Comedy");

        //assert
        assertThat(itemList).size().isEqualTo(1);
    }


}