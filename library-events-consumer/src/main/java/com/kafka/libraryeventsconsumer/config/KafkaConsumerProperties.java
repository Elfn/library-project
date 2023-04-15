package com.kafka.libraryeventsconsumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 14/04/2023
 */
@Data
@Configuration
@EnableKafka
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaConsumerProperties {
  private String groupId;
}
