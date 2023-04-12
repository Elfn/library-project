package com.kafka.libraryeventsproducer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
@Configuration
@ConfigurationProperties(prefix = "cluster")
public class ConfigProperties {

  private List<String> topics;

  // Setters and getters are mandatory
  // to enable property extraction
  public void setTopics(List<String> topics) {
    this.topics = topics;
  }
  public  List<String> getTopics() {
    return topics;
  }

}
