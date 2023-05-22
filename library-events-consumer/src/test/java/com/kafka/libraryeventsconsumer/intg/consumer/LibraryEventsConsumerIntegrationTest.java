package com.kafka.libraryeventsconsumer.intg.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.kafka.libraryeventsconsumer.config.KafkaConsumerProperties;
import com.kafka.libraryeventsconsumer.consumer.LibraryEventsConsumer;
import com.kafka.libraryeventsconsumer.entity.BookEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventTypeEntity;
import com.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;
import com.kafka.libraryeventsconsumer.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 17/04/2023
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EmbeddedKafka(topics = {"library-events","library-events.RETRY","library-events.DLT"}, partitions = 3)
@AutoConfigureMockMvc // enable and configure auto-configuration of Mock-Mvc.
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class) // declare a custom display name generator for the annotated test class.
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}","spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"/*,"retryListener.startup=false"*/})
public class LibraryEventsConsumerIntegrationTest {

  @Autowired
  private  EmbeddedKafkaBroker kafkaBroker;
  @Autowired
  private  KafkaTemplate<Integer, String> kafkaTemplate;
  @Autowired
  private LibraryEventsRepository repository;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private KafkaConsumerProperties properties;

  private Consumer<Integer,String> consumer;

  @Autowired
  private Gson gson;

  @Autowired
  // Permet de gerer les points de terminaison des consommateurs et des producteurs de Kafka
  private  KafkaListenerEndpointRegistry endpointRegistry;

  // Dépendances externes qui vont nous permettre de savoir:
  // Si la donnée à ete recuperée par le consumer
  @SpyBean
  LibraryEventsConsumer consumerSpy;
  // Si la donnée à ete persistée en BD via de Service
  @SpyBean
  LibraryEventsService serviceSpy;

  @Autowired
  EmbeddedKafkaBroker embeddedKafkaBroker;



  @BeforeEach
  void setUp() {

    // Ayant maintenant 2 topics cette methode Permet de ne consumer que les partitions du topic "library-events" lors du test
    var container = endpointRegistry.getListenerContainers().stream()
      .filter(messageListenerContainer -> Objects.equals(messageListenerContainer.getGroupId(),"library-events-listener-group"))
      .collect(Collectors.toList()).get(0);
    ContainerTestUtils.waitForAssignment(container, kafkaBroker.getPartitionsPerTopic());

 // ----------------------------------------------------------------------------------------------------------
//    // MessageListenerContainer est un composant clé dans les applications Kafka qui permet aux
//    // consommateurs de lire des messages à partir des topics Kafka
  //  for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()){

//      // permet d'attendre que toutes les partitions d'un topic Kafka soient affectées
//      // à des consommateurs avant de poursuivre les tests
  //    ContainerTestUtils.waitForAssignment(messageListenerContainer, kafkaBroker.getPartitionsPerTopic());
   // }
// ----------------------------------------------------------------------------------------------------------

  }

  @AfterEach
  void tearDown() {
    // Supprimer les données préexistantes avant de relancer les tests
    repository.deleteAll();
    consumer.close();
  }

  @Test
  @Order(1)
  void postNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {

    // Given
    String json = "{\"libraryEventId\":999,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    // Ici le producer depose la donnee dans le topic par defaut (library-events)
    kafkaTemplate.sendDefault(json).get();

    // When
    // Marque un temps d'attente pour permettre
    // au threads de s'executer tour à tour
    CountDownLatch latch = new CountDownLatch(1);
    // Le thead attend 3 secondes le temps que le
    // compteur (1) du CountDownLatch soit décrémenté
    // Jusqu'a (0)
    latch.await(3, TimeUnit.SECONDS);

    // Then
    // Verifier si le consumer est appelè 2 fois lors du test
    verify(consumerSpy, times(2)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 2 fois lors du test
    verify(serviceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

    // Faire des assertions sur le contenus de la table "LIBRARY_EVENT_ENTITY" dans la BD
   List<LibraryEventEntity> entityList = (List<LibraryEventEntity>) repository.findAll();
   assert entityList.size() == 0;
   entityList.forEach(libraryEvent -> {
     assert libraryEvent.getLibraryEventId() != null;
     assertEquals(123, libraryEvent.getBook().getBookId());
   });

  }

  @Test
  @Order(3)
  void updateLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {

    // Given:

    // Ajouter avant de modifier
    String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    LibraryEventEntity libraryEvent = objectMapper.readValue(json, LibraryEventEntity.class);
    libraryEvent.getBook().setLibraryEvent(libraryEvent);
    repository.save(libraryEvent);

    // Modifier apres ajout
    BookEntity updatedBook = BookEntity.builder().bookId(123).bookName("Kafka Using Spring Boot 3.x").bookAuthor("Dilip").build();
    libraryEvent.setLibraryEventType(LibraryEventTypeEntity.UPDATE);
    libraryEvent.setBook(updatedBook);
    // Recuperation de la données modifiée pour pouvoir faire l'assertion
    String updatedJson = objectMapper.writeValueAsString(libraryEvent);
    // Dans le cas de l'update il faut passer l'id (Key)
    kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();

    // When
    // Marque un temps d'attente pour permettre
    // au threads de s'executer tour à tour
    CountDownLatch latch = new CountDownLatch(1);
    // Le thead attend 3 secondes le temps que le
    // compteur (1) du CountDownLatch soit décrémenté
    // Jusqu'a (0)
    latch.await(3, TimeUnit.SECONDS);

    // Then
    // Verifier si le consumer est appelè 1 fois lors du test
    verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 1 fois lors du test
    verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

    LibraryEventEntity persistedLibraryEvent = repository.findById(libraryEvent.getLibraryEventId()).get();
    assertEquals("Kafka Using Spring Boot 3.x", persistedLibraryEvent.getBook().getBookName());


  }


  @Test
  @Order(4)
  void invalidUpdateWithLibraryEventIdEqualsToNull() throws IOException, ExecutionException, InterruptedException {

    // Given:

    // Ajouter avant de modifier
    String json = "{\"libraryEventId\":null,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    LibraryEventEntity libraryEvent = gson.fromJson(json, LibraryEventEntity.class);
    kafkaTemplate.sendDefault(json).get();;


    // When
    // Marque un temps d'attente pour permettre
    // au threads de s'executer tour à tour
    CountDownLatch latch = new CountDownLatch(1);
    // Le thead attend 3 secondes le temps que le
    // compteur (1) du CountDownLatch soit décrémenté
    // Jusqu'a (0)
    latch.await(3, TimeUnit.SECONDS);

    // Then
    // Verifier si le consumer est appelè 1 fois lors du test
    verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 1 fois lors du test
    verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

    assert libraryEvent.getLibraryEventId() == null;

    Map<String,Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group2", "true", embeddedKafkaBroker));
    consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
    embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, properties.getLibraryEventsDLT());

    ConsumerRecord<Integer,String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, properties.getLibraryEventsDLT());
    // Thread.sleep(5); // Wait for getSingleRecord method
    String value = consumerRecord.value();
    assertEquals(json,value);
    log.info("Record successfully get: {}, from: {}", consumerRecord.value(), consumerRecord.topic());

  }

  @Test
  @Order(5)
  void invalidUpdateWithLibraryEventIdEqualsTo999() throws IOException, ExecutionException, InterruptedException {

    // Given:

    // Ajouter avant de modifier
    String json = "{\"libraryEventId\":999,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    LibraryEventEntity libraryEvent = gson.fromJson(json, LibraryEventEntity.class);
    kafkaTemplate.sendDefault(json).get();;


    // When
    // Marque un temps d'attente pour permettre
    // au threads de s'executer tour à tour
    CountDownLatch latch = new CountDownLatch(1);
    // Le thead attend 3 secondes le temps que le
    // compteur (1) du CountDownLatch soit décrémenté
    // Jusqu'a (0)
    latch.await(3, TimeUnit.SECONDS);

    // Then
    // Verifier si le consumer est appelè 1 fois lors du test
    verify(consumerSpy, times(2)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 1 fois lors du test
    verify(serviceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

    assert libraryEvent.getLibraryEventId() == 999;

    Map<String,Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
    consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
    embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, properties.getLibraryEventsRetry());

    ConsumerRecord<Integer,String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, properties.getLibraryEventsRetry());
    // Thread.sleep(5); // Wait for getSingleRecord method
    final File jsonFile = new ClassPathResource("libraryEvent.json").getFile();
    final String expectedRecord = Files.readString(jsonFile.toPath()).trim();
    String value = consumerRecord.value();
    assertEquals(expectedRecord,value);
    log.info("Record successfully get: {}, from: {}", consumerRecord.value(), consumerRecord.topic());
  }

  @Test
  @Order(2)
  @SqlGroup({
    @Sql(value = "classpath:reset-data.sql", executionPhase = BEFORE_TEST_METHOD),
    @Sql(value = "classpath:user-data.sql", executionPhase = BEFORE_TEST_METHOD)
  })
  void should_use_real_db_with_testcontainers() throws IOException, ExecutionException, InterruptedException {

    // Given
    final File jsonFile = new ClassPathResource("libraryEvent.json").getFile();
    final String libraryEventJson = Files.readString(jsonFile.toPath());
    LibraryEventEntity libraryEvent = gson.fromJson(libraryEventJson, LibraryEventEntity.class);
    kafkaTemplate.sendDefault(libraryEventJson).get();
    int total = 5;

    // When
    // Marque un temps d'attente pour permettre
    // au threads de s'executer tour à tour
    CountDownLatch latch = new CountDownLatch(1);
    // Le thead attend 3 secondes le temps que le
    // compteur (1) du CountDownLatch soit décrémenté
    // Jusqu'a (0)
    latch.await(3, TimeUnit.SECONDS);

    // Then
    // Verifier si le consumer est appelè 1 fois lors du test
    verify(consumerSpy, times(5)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 1 fois lors du test
    verify(serviceSpy, times(5)).processLibraryEvent(isA(ConsumerRecord.class));

    // Verify if we have 5 items
    assert repository.count() == total;
    log.info("Total amount of records {} ", repository.count());


  }

}
