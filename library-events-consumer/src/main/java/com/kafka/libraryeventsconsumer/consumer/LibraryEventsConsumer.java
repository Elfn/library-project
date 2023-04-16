package com.kafka.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.libraryeventsconsumer.config.KafkaConsumerProperties;
import com.kafka.libraryeventsconsumer.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 14/04/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

  private final LibraryEventsService service;

  @KafkaListener(topics = {"library-events"})
  public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
     log.info("ConsumerRecord : {}", consumerRecord);
     service.processLibraryEvent(consumerRecord);
  }

}
