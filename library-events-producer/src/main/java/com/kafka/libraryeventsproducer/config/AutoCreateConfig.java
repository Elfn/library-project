package com.kafka.libraryeventsproducer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 07/04/2023
 */
@Configuration
@Profile("local")
public class AutoCreateConfig {

//  @Bean
//  public NewTopic libraryEvents(){
//
//    return TopicBuilder.name("library-events")
//        .partitions(3)
//        .replicas(3)
//        .build();
//
//  }


  @Bean
  public List<NewTopic> libraryEvents(){

    return List.of(
           TopicBuilder.name("library-events")
             .partitions(3)
             .replicas(3)
             .build(),
          TopicBuilder.name("library-events.DLT")
            .partitions(4)
            .replicas(1)
            .build(),
          TopicBuilder.name("library-events.RETRY")
            .partitions(3)
            .replicas(3)
            .build()
      );

  }

}
