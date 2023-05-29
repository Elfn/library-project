package com.kafka.libraryeventsconsumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 14/04/2023
 */
@Configuration
@Slf4j
@EnableKafka
public class LibraryEventsConsumerConfig {

  @Autowired
  private KafkaConsumerProperties properties;

  @Autowired
  private KafkaTemplate template;





  public DeadLetterPublishingRecoverer publishingRecoverer(){

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
      (r, e) -> {
        log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
        if (e.getCause() instanceof RecoverableDataAccessException) {
          log.info("The topic joined is : {} ", r.topic());
          return new TopicPartition(properties.getLibraryEventsRetry(), r.partition());
        }
        else {
          log.info("The topic joined is : {} ", r.topic());
          return new TopicPartition(properties.getLibraryEventsDLT(), r.partition());
        }
      });
    //CommonErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 2L));
    return recoverer;

  }

  @KafkaListener(topics = "library-events.DLT", groupId = "groupe_id")
  public void lireMessage(String message) {
    System.out.println("Message reçu : " + message);
  }


  public DefaultErrorHandler errorHandler(){
    // Retry consumming operation twice(2) with a delay of 1s
    var fixedBackOff = new FixedBackOff(1000L, 2);

    // Retry process is not applied when we meet that exception
     var exceptionsToIgnore = List.of(IllegalArgumentException.class);

    // Retry process is applied when we meet that exception
    //var exceptionsToRetry = List.of(RecoverableDataAccessException.class);


    // Retry failed Records with ExponentialBackOff
    var exponentialBackOff = new ExponentialBackOffWithMaxRetries(properties.getMaxRetries());
    exponentialBackOff.setInitialInterval(properties.getInitialInterval());
    exponentialBackOff.setMultiplier(properties.getMultiplier());
    exponentialBackOff.setMaxInterval(properties.getMaxInterval());


    var errorHandler = new DefaultErrorHandler(
      //fixedBackOff
      publishingRecoverer(),
      exponentialBackOff
    );

    // To retry or not only specific exceptions using retryPolicy
     exceptionsToIgnore.forEach(errorHandler::addNotRetryableExceptions);
    // exceptionsToRetry.forEach(errorHandler::addRetryableExceptions);

    // To monitor each retry
    errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
      log.info("Failed Record in Retry Listener, Exception : {}, deliveryAttempt: {}", ex.getMessage(), deliveryAttempt);
    }));

    return errorHandler;
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ConsumerFactory<Object, Object> kafkaConsumerFactory) {
    ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, kafkaConsumerFactory);

    // 3 concurrent threads to parallely process the records
    factory.setConcurrency(3);

    // To implement custom error handling
    factory.setCommonErrorHandler(errorHandler());

    //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    return factory;
  }

}


