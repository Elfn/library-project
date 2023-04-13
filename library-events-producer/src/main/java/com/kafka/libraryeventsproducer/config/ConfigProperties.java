package com.kafka.libraryeventsproducer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.List;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
@Configuration
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
