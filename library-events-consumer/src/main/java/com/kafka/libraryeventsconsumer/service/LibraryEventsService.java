package com.kafka.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 16/04/2023
 */
public interface LibraryEventsService {
  public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException;
}
