package com.sky.csc.title.discovery.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.util.Collections.unmodifiableList;

@ApiIgnore
@RestController
public class TestDataController {

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

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();

    @GetMapping("/title-discovery/load")
    public HttpStatus load(
            @RequestParam(value = "numberOfItems", required = true) String numberOfItems,
            @RequestParam(value = "numberOfOffers", required = true) String numberOfOffers,
            @RequestParam(value = "numberOfTerms", required = true) String numberOfTerms){

        produce(bootstrapServers, numberOfItems, numberOfOffers, numberOfTerms);

        return HttpStatus.OK;

    }

    private void produce(String... inputParameterArray){

        try (final Producer<String, byte[]> producer = createProducer(inputParameterArray[0])) {

            int numberOfItems = Integer.parseInt(inputParameterArray[1]);
            int numberOfOffers = Integer.parseInt(inputParameterArray[2]);
            int numberOfTerms = Integer.parseInt(inputParameterArray[3]);

            IntStream.range(0, numberOfItems)

                    .forEach(value -> {

                        System.out.println("Number of Items: " + numberOfItems);

                        final String publishItemId = publishItem(producer);

                        System.out.println("Number of Offers: " + numberOfOffers);

                        IntStream.range(0, numberOfOffers)

                                .forEach(value1 -> {

                                    String offerId = publishOffer(producer, publishItemId);

                                    System.out.println("Number of Terms: " + numberOfTerms);

                                    IntStream.range(0, numberOfTerms)

                                            .forEach(value2 -> {

                                                publishTerm(producer, offerId);

                                            });
                                });
                    });


        }
    }

    private static Producer<String, byte[]> createProducer(String bootstrapServers) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "kiro");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return new KafkaProducer<>(config);
    }

    private static final List<String> itemType = unmodifiableList(Arrays.asList(
            "Programme", "Series", "Documentary", "Kids", "Game Show"));

    private static final List<String> subType = unmodifiableList(Arrays.asList(
            "Movie", "Special", "World", "Technology", "Historical", "Equestrian"));

    private static final List<String> genre = unmodifiableList(Arrays.asList(
            "Action", "Comedy", "BollyWood"));

    /**
     * Build and publish stub item
     *
     * @param producer the kafka producer
     * @return the itemId
     * @throws Exception should any error happen during processing
     */
    private String publishItem(Producer<String, byte[]> producer) {

        ObjectNode item = objectMapper.createObjectNode();
        item.put("itemId", UUID.randomUUID().toString());
        item.put("item-type", itemType.get(random.nextInt(itemType.size())));
        item.put("sub-type", subType.get(random.nextInt(subType.size())));
        item.put("genre", genre.get(random.nextInt(genre.size())));

        item.put("alias", "item");

        byte[] itemAsJson = new byte[0];
        try {
            itemAsJson = objectMapper.writeValueAsBytes(item);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        String itemId = item.get("itemId").textValue();
        ProducerRecord<String, byte[]> itemRecord = new ProducerRecord<>(itemTopic, itemId, itemAsJson);
        RecordMetadata sentItemMetaData = null;
        try {
            sentItemMetaData = producer.send(itemRecord).get();
            log(item, itemId, sentItemMetaData);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return itemId;
    }

    /**
     * Build and publish stub offeredItem
     *
     * @param producer the kafka producer
     * @return the offeredItemId
     * @throws Exception should any error happen during processing
     */
    private void publishTerm(Producer<String, byte[]> producer, String offerId) {

        ObjectNode term = objectMapper.createObjectNode();
        term.put("termId", UUID.randomUUID().toString());
        term.put("offerId", offerId);

        term.put("alias", "term");

        byte[] termAsJson = new byte[0];
        try {
            termAsJson = objectMapper.writeValueAsBytes(term);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        String termId = term.get("termId").textValue();
        ProducerRecord<String, byte[]> termRecord = new ProducerRecord<>(termTopic, termId, termAsJson);
        RecordMetadata sentOfferedItemMetaData = null;
        try {
            sentOfferedItemMetaData = producer.send(termRecord).get();
            log(term, termId, sentOfferedItemMetaData);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build and publish stub offer
     *
     * @param producer the kafka producer
     * @param itemId   the item ID
     * @throws Exception should any error happen during processing
     */
    private String publishOffer(Producer<String, byte[]> producer, String itemId) {

        ObjectNode offer = objectMapper.createObjectNode();
        offer.put("offerId", UUID.randomUUID().toString());
        offer.put("itemId", itemId);
        offer.put("startDate", ZonedDateTime.now().minusDays(random.nextInt(90)).toString());
        offer.put("endDate", ZonedDateTime.now().plusDays(random.nextInt(90)).toString());
        offer.put("alias", "offer");

        byte[] offerAsJson = new byte[0];
        try {
            offerAsJson = objectMapper.writeValueAsBytes(offer);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        String offerId = offer.get("offerId").textValue();
        ProducerRecord<String, byte[]> offerRecord = new ProducerRecord<>(offerTopic, offerId, offerAsJson);

        RecordMetadata sentOfferMetaData = null;
        try {
            sentOfferMetaData = producer.send(offerRecord).get();
            log(offer, offerId, sentOfferMetaData);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return offerId;
    }

    private static void log(ObjectNode alias, String key, RecordMetadata sentItemMetaData) {
        System.out.format("Published %s/%s/%s (key=%s:%s",
                sentItemMetaData.topic(),
                sentItemMetaData.partition(),
                sentItemMetaData.offset(), key, alias);
    }
}

