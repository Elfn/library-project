package com.kafka.libraryeventsconsumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 15/04/2023
 */
//@Component
@Slf4j
public class LibraryEventsConsumerManualOffset implements AcknowledgingMessageListener<Integer,String> {

  // To manually commit the offsets
  @Override
  @KafkaListener(topics = {"library-events"})
  public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
    log.info("ConsumerRecord : {}", consumerRecord);
    acknowledgment.acknowledge();
  }


}
