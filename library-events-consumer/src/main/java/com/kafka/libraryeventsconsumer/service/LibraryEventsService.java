package com.kafka.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.libraryeventsconsumer.entity.FailureRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 16/04/2023
 */
public interface LibraryEventsService {
  public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException;
  public FailureRecord saveFailedRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status);
}
