package com.kafka.libraryeventsconsumer.intg.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsconsumer.consumer.LibraryEventsConsumer;
import com.kafka.libraryeventsconsumer.entity.BookEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventTypeEntity;
import com.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;
import com.kafka.libraryeventsconsumer.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.awt.print.Book;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 17/04/2023
 */
@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}","spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
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
  // Permet de gerer les points de terminaison des consommateurs et des producteurs de Kafka
  private  KafkaListenerEndpointRegistry endpointRegistry;

  // Dépendances externes qui vont nous permettre de savoir:
  // Si la donnée à ete recuperée par le consumer
  @SpyBean
  LibraryEventsConsumer consumerSpy;
  // Si la donnée à ete persistée en BD via de Service
  @SpyBean
  LibraryEventsService serviceSpy;



  @BeforeEach
  void setUp() {

    // MessageListenerContainer est un composant clé dans les applications Kafka qui permet aux
    // consommateurs de lire des messages à partir des topics Kafka
    for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()){

      // permet d'attendre que tous les partitions d'un topic Kafka soient affectées
      // à des consommateurs avant de poursuivre les tests
      ContainerTestUtils.waitForAssignment(messageListenerContainer, kafkaBroker.getPartitionsPerTopic());
    }
  }

  @AfterEach
  void tearDown() {
    // Supprimer les données préexistantes avant de relancer les tests
    repository.deleteAll();
  }

  @Test
  void postNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {

    // Given
    String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
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
    // Verifier si le consumer est appelè 1 fois lors du test
    verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    // Verifier si le service est appelè 1 fois lors du test
    verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

    // Faire des assertions sur le contenus de la table "LIBRARY_EVENT_ENTITY" dans la BD
   List<LibraryEventEntity> entityList = (List<LibraryEventEntity>) repository.findAll();
   assert entityList.size() == 1;
   entityList.forEach(libraryEvent -> {
     assert libraryEvent.getLibraryEventId() != null;
     assertEquals(123, libraryEvent.getBook().getBookId());
   });

  }

  @Test
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
  void invalidUpdateLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {

    // Given:

    // Ajouter avant de modifier
    String json = "{\"libraryEventId\":null,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
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



  }

}
