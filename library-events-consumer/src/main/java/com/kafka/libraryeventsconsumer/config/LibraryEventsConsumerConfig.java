package com.kafka.libraryeventsconsumer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
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

  public DefaultErrorHandler errorHandler(){
    // Retry consumming operation twice(2) with a delay of 1s
    var fixedBackOff = new FixedBackOff(1000L, 2);

   // var exceptionsToIgnore = List.of(IllegalArgumentException.class);
    var exceptionsToRetry = List.of(RecoverableDataAccessException.class);


    // Retry failed Records with ExponentialBackOff
    var exponentialBackOff = new ExponentialBackOffWithMaxRetries(properties.getMaxRetries());
    exponentialBackOff.setInitialInterval(properties.getInitialInterval());
    exponentialBackOff.setMultiplier(properties.getMultiplier());
    exponentialBackOff.setMaxInterval(properties.getMaxInterval());


    var errorHandler = new DefaultErrorHandler(
      //fixedBackOff
      exponentialBackOff
    );

    // To retry or not only specific exceptions using retryPolicy
    // exceptionsToIgnore.forEach(errorHandler::addNotRetryableExceptions);
    exceptionsToRetry.forEach(errorHandler::addRetryableExceptions);

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


