package com.sky.csc.title.discovery.service;

import com.sky.csc.title.discovery.util.JsonUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

import static com.sky.csc.title.discovery.util.JsonUtils.Constants.*;
import static org.apache.commons.lang.StringUtils.EMPTY;

@Service
public class TitleTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(TitleTopology.class);

    private static final String EARLIEST = "earliest";
    private KafkaStreams streams;
    private final JsonUtils jsonUtils;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.item}")
    private String itemTopic;

    @Value("${kafka.topic.offer}")
    private String offerTopic;

    @Value("${kafka.topic.term}")
    private String termTopic;

    @Value("${kafka.topic.title}")
    private String titleTopic;

    @Value("${kafka.application.id}")
    private String applicationId;

    public TitleTopology(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    @PostConstruct
    public void runStream() {

        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, String> items = builder.table(itemTopic);
        KTable<String, String> offers = builder.table(offerTopic);
        KTable<String, String> terms = builder.table(termTopic);

        KTable<String, String> aggregatedTermsList =

                terms.groupBy((key, value) ->
                        KeyValue.pair(jsonUtils.getKeyFromJson(OFFER_ID_JSON_PROPERTY, value), value))
                        .aggregate(

                                () -> EMPTY,

                                (key, value, aggregate) -> aggregate.concat(value),

                                (key, value, aggregate) -> aggregate.replace(value, EMPTY))

                        .mapValues(jsonUtils::formatToCSVArray);


        KTable<String, String> offerWithTerms = offers

                .leftJoin(aggregatedTermsList, (parentNodeAsString, childNodeArrayAsString) ->

                        jsonUtils.combineParentWithChildrenNodes(parentNodeAsString, childNodeArrayAsString, TERMS_JSON_PROPERTY));

        KTable<String, String> aggregatedOffers =

                offerWithTerms.groupBy((key, value) ->
                        KeyValue.pair(jsonUtils.getKeyFromJson(ITEM_ID_JSON_PROPERTY, value), value))
                        .aggregate(

                                () -> EMPTY,

                                (key, value, aggregate) -> aggregate.concat(value),

                                (key, value, aggregate) -> aggregate.replace(value, EMPTY))

                        .mapValues(jsonUtils::formatToCSVArray);

        KTable<String, String> aggregatedItems = items
                .leftJoin(aggregatedOffers, (parentNodeAsString, childNodeArrayAsString) ->
                        jsonUtils.combineParentWithChildrenNodes(parentNodeAsString, childNodeArrayAsString, OFFERS_JSON_PROPERTY));


        aggregatedItems
                .toStream()
                .peek((key, value) -> System.out.format("Output key to title is: %s with value of: %s%n", key, value))
                .to(titleTopic);


        Topology topology = builder.build();

        LOGGER.info("topology description " + topology.describe());

        streams = new KafkaStreams(topology, getPropertiesForStream());

        streams.start(); }



    private Properties getPropertiesForStream() {

        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);

        String stringSerdeName = Serdes.String().getClass().getName();
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerdeName);
        streamsConfiguration.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 10);
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerdeName);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        return streamsConfiguration;
    }


    @PreDestroy
    public void closeStream() {
        streams.close();
        streams.cleanUp();
    }
}

