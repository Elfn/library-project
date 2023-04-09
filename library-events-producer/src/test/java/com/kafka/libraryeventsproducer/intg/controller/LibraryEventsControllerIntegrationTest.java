package com.kafka.libraryeventsproducer.intg.controller;

import com.kafka.libraryeventsproducer.domain.Book;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.domain.LibraryEventType;
import com.kafka.libraryeventsproducer.dto.LibraryEventResponse;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
/*
 * @SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT) :
 *1- L'annotation @SpringBootTest est utilisée pour déclencher un test d'intégration avec un contexte Spring Boot complet.
 *2- L'attribut "webEnvironment" permet de configurer l'environnement de test pour les applications web.
 *3- En définissant la valeur à "RANDOM_PORT", Spring Boot démarre un serveur sur un port aléatoire afin d'éviter les conflits de ports lors des tests.
 *
 * */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}","spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  EmbeddedKafkaBroker embeddedKafkaBroker;

  private Consumer<Integer,String> consumer;

  @BeforeEach
  void setUp() {
    Map<String,Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
    consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
    embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

  @Test
  @Timeout(5)
  void postLibraryEvent() throws InterruptedException{

    //Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).libraryEventType(LibraryEventType.NEW).book(book).build();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_JSON.toString());
    HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

    //When
    ResponseEntity<LibraryEventResponse> response = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEventResponse.class);

    //Then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ConsumerRecord<Integer,String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
   // Thread.sleep(5); // Wait for getSingleRecord method
    String expectedRecord = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    String value = consumerRecord.value();
    assertEquals(expectedRecord,value);
  }

}
