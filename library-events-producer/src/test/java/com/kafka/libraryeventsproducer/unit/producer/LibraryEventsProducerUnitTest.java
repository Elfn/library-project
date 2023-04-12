package com.kafka.libraryeventsproducer.unit.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsproducer.domain.Book;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.producer.LibraryEventProducerWithSend;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LibraryEventsProducerUnitTest {

  @Mock
  KafkaTemplate<Integer, String> kafkaTemplate;

  @Spy
  ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  LibraryEventProducerWithSend eventProducer;

  @Test
  void libraryEventProducerWithSend_failure() throws ExecutionException, InterruptedException {

    // Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
    CompletableFuture future = new CompletableFuture();

    future.completeExceptionally(new RuntimeException("Exception Calling Kafka"));
    lenient().when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
    //when

    assertThrows(Exception.class, ()->eventProducer.sendLibraryEventAsynchronousWayReturnCompletableFuture(libraryEvent).get());

    //then
  }

  @Test
  void libraryEventProducerWithSend_success() throws ExecutionException, InterruptedException, JsonProcessingException {

    // Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
    String record = objectMapper.writeValueAsString(libraryEvent);
    CompletableFuture future = new CompletableFuture();

    ProducerRecord<Integer,String> producerRecord = new ProducerRecord("library-events", libraryEvent.getLibraryEventId(), record);
    RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events",1), 1,1,342, 1, 2);

    SendResult<Integer, String> sendResult = new SendResult<Integer,String>(producerRecord,recordMetadata);

    //when
    future.complete(sendResult);
    lenient().when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
    CompletableFuture<SendResult<Integer,String>> completableFuture = eventProducer.sendLibraryEventAsynchronousWayReturnCompletableFuture(libraryEvent);

    //then
    SendResult<Integer,String> result = completableFuture.get();
    assertEquals(result.getRecordMetadata().partition(),1);
  }

}
