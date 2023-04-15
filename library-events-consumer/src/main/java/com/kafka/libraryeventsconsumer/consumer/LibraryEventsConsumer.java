package com.kafka.libraryeventsconsumer.consumer;

import com.kafka.libraryeventsconsumer.config.KafkaConsumerProperties;
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
public class LibraryEventsConsumer {

  @KafkaListener(topics = {"library-events"})
  public void onMessage(ConsumerRecord<Integer,String> consumerRecord){
     log.info("ConsumerRecord : {}", consumerRecord);
  }

}
