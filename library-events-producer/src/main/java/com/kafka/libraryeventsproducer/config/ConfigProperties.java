package com.kafka.libraryeventsproducer.config;

import lombok.Data;
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
@Data
@Configuration
@ConfigurationProperties(prefix = "cluster.topics")
public class ConfigProperties {

  private String libraryEvents;


}
