package com.sky.csc.title.discovery;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.mock.BucketConfiguration;
import com.couchbase.mock.CouchbaseMock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.kafka.test.KafkaTestCluster;
import com.salesforce.kafka.test.KafkaTestUtils;
import com.sky.csc.title.discovery.service.TitleDiscoveryService;
import com.sky.csc.title.discovery.util.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.couchbase.client.java.query.Select.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
public class TitleDiscoveryIT {

    private static final Logger logger = LoggerFactory.getLogger(TitleDiscoveryIT.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonUtils jsonUtils = new JsonUtils(objectMapper);

    private final static BucketConfiguration bucketConfiguration = new BucketConfiguration();
    public static final String TDS_DATA = "tds-data";
    private static CouchbaseMock couchbaseMock;
    private static int carrierPort;
    private static int httpPort;
    private static KafkaTestUtils utils;
    private static KafkaTestCluster kafkaTestCluster;

    @Autowired
    TitleDiscoveryService titleDiscoveryService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Bucket titleBucket;

    @BeforeClass
    public static void up() throws Exception {

        createMock();
        httpPort = couchbaseMock.getHttpPort();
        carrierPort = couchbaseMock.getCarrierPort(TDS_DATA);
        startKafkaService(1);

    }

    @After
    public void after() throws Exception {

        if (couchbaseMock != null) {
            couchbaseMock.stop();
        }

        kafkaTestCluster.stop();

    }

    @TestConfiguration
    static class TitleDiscoveryTestConfig {

        @Bean
        public Cluster couchbaseCluster(){
            return CouchbaseCluster.create
                    (DefaultCouchbaseEnvironment.builder()
                    .bootstrapCarrierDirectPort(carrierPort)
                    .bootstrapHttpDirectPort(httpPort)
                    .build(), "couchbase://127.0.0.1")
                    .authenticate(TDS_DATA, TDS_DATA);
        }

        @Bean(destroyMethod = "close")
        public Bucket titleBucket() {

            Bucket bucket = couchbaseCluster().openBucket(TDS_DATA);

           // createIndexes(bucket);

            return bucket;
        }

        private void createIndexes(Bucket bucket) {

            bucket.query(N1qlQuery.simple(
                    "CREATE PRIMARY INDEX ON `" + bucket.name() + "`;"
            ));
        }
    }


    private static void startKafkaService(final int clusterSize) throws Exception {

        Properties brokerProperties = new Properties();
        brokerProperties.setProperty("port", "9092");
        brokerProperties.setProperty("cleanup.policy","compact");

        kafkaTestCluster = new KafkaTestCluster(clusterSize, brokerProperties);
        kafkaTestCluster.start();

        utils = new KafkaTestUtils(kafkaTestCluster);

        utils.createTopic("items", clusterSize, (short) clusterSize);
        utils.createTopic("offers", clusterSize, (short) clusterSize);
        utils.createTopic("terms", clusterSize, (short) clusterSize);
        utils.createTopic("titles", clusterSize, (short) clusterSize);

        logger.info("Cluster started at: {}", kafkaTestCluster.getKafkaConnectString());
    }

    @Test
    public void getItemsByGenreActionReturnsListOfGenres() throws IOException, InterruptedException {

        ResponseEntity<String> populate = restTemplate.getForEntity(
                "http://localhost:8080/title-discovery/load?items=5&offers=2&terms=2&genre=Action",
                String.class);

        assertThat(populate.getStatusCode()).isEqualTo(HttpStatus.OK);

        try (final KafkaConsumer<String, String> kafkaConsumer =
                     utils.getKafkaConsumer(StringDeserializer.class, StringDeserializer.class)) {

            final List<TopicPartition> topicPartitionList = new ArrayList<>();

            for (final PartitionInfo partitionInfo : kafkaConsumer.partitionsFor("titles")) {
                topicPartitionList.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
            }

            kafkaConsumer.assign(topicPartitionList);
            kafkaConsumer.seekToBeginning(topicPartitionList);

            ConsumerRecords<String, String> records;

            TimeUnit.SECONDS.sleep(45l);

            Set<String> keys = new HashSet<>();

            do {

                records = kafkaConsumer.poll(Duration.ofSeconds(20L));
                logger.info("Found {} records in kafka", records.count());

                for (ConsumerRecord<String, String> record : records) {

                    keys.add(record.key());

                    System.out.format("The key: %s and the value: %s%n", record.key(), record.value());
                    JsonDocument title = JsonDocument.create(record.key(), JsonObject.fromJson(record.value()));
                    titleBucket.upsert(title);

                }
            }

            while (!records.isEmpty());

            keys.forEach(s -> System.out.println("\n\nXXXXXXXX "+ titleBucket.get(s) + " XXXXXXX"));


        }




    }

    private static void createMock() throws Exception {

        bucketConfiguration.type = com.couchbase.mock.Bucket.BucketType.COUCHBASE;
        ArrayList<BucketConfiguration> configList = new ArrayList<BucketConfiguration>();
        bucketConfiguration.numVBuckets = 1024;
        bucketConfiguration.numReplicas = 1;
        bucketConfiguration.numNodes = 1;
        bucketConfiguration.name = TDS_DATA;
        bucketConfiguration.password = TDS_DATA;

        configList.add(bucketConfiguration);

        couchbaseMock = new CouchbaseMock(8091, configList);
        couchbaseMock.start();
        couchbaseMock.waitForStartup();
    }
}
