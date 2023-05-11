package com.kafka.libraryeventsconsumer.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 14/04/2023
 */
@Data
@Configuration
@EnableKafka
@Profile("local")
// @ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaConsumerProperties {
  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Value("${spring.kafka.consumer.initialInterval}")
  private Long initialInterval;

  @Value("${spring.kafka.consumer.maxInterval}")
  private Long maxInterval;

  @Value("${spring.kafka.consumer.maxRetries}")
  private int maxRetries;

  @Value("${spring.kafka.consumer.multiplier}")
  private double multiplier;
}
