package com.kafka.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsproducer.config.ConfigProperties;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 07/04/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventProducerWithSend {

     private final KafkaTemplate<Integer, String> kafkaTemplate;
     private final ObjectMapper objectMapper;
     private final ConfigProperties configProperties;

     // Send record asynchronously
     public void sendLibraryEventAsynchronousWay(LibraryEvent libraryEvent) throws JsonProcessingException {

            Integer key = libraryEvent.getLibraryEventId();
            String value = objectMapper.writeValueAsString(libraryEvent);
            String topic = configProperties.getTopics().get(0);

       ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, topic);

       CompletableFuture<SendResult<Integer,String>> completableFuture = kafkaTemplate.send(producerRecord);
       completableFuture.whenComplete((result, exception) -> {
         if (exception == null) {
           // Votre code de traitement en cas de succès
           handleSuccess(key,value,result);
         } else {
           // Votre code de traitement en cas d'échec
           handleFailure(key, value, exception);
         }
       });
     }

  public CompletableFuture<SendResult<Integer,String>> sendLibraryEventAsynchronousWayReturnCompletableFuture(LibraryEvent libraryEvent) throws JsonProcessingException {

    Integer key = libraryEvent.getLibraryEventId();
    String value = objectMapper.writeValueAsString(libraryEvent);

    ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, "library-events");

    CompletableFuture<SendResult<Integer,String>> completableFuture = kafkaTemplate.send(producerRecord);
    completableFuture.whenComplete((result, exception) -> {
      if (exception == null) {
        // Votre code de traitement en cas de succès
        handleSuccess(key,value,result);
      } else {
        // Votre code de traitement en cas d'échec
        handleFailure(key, value, exception);
      }
    });
    return completableFuture;
  }


  // Send record synchronously
  public SendResult<Integer, String> sendLibraryEventSynchronousWay(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
    Integer key = libraryEvent.getLibraryEventId();
    String value = objectMapper.writeValueAsString(libraryEvent);
    String topic = configProperties.getTopics().get(0);
    SendResult<Integer, String> sendResult = null;
    ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, topic);

    // Wait until the future is resolved to onSuccess or onFailure
    try {
       sendResult = kafkaTemplate.send(producerRecord).get(2, TimeUnit.SECONDS);
    } catch (ExecutionException | InterruptedException ex) {
      log.error("InterruptedException/ExecutionException sending the message, the exception is {}", ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      log.error("Exception sending the message, the exception is {}", ex.getMessage());
      throw ex;
    }
    return sendResult;
  }

  private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
       log.info("Message sent successfully for the key {}, and the value {}, partition is {} ", key, value, result.getRecordMetadata().partition());
  }

  private void handleFailure(Integer key, String value, Throwable ex) {
    log.error("Error sending the message, the exception is {}", ex.getMessage());
    try {
      throw ex;
    } catch (Throwable e) {
      log.error("Error in onFailure: {}", e.getMessage());
    }
  }

  private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {

       // Build header
    List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

    return new ProducerRecord<>(topic, null, key, value, recordHeaders);
  }

}
