package com.kafka.libraryeventsconsumer.intg.service;

import com.google.gson.Gson;
import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import com.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 22/04/2023
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc // enable and configure auto-configuration of Mock-Mvc.
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class) // declare a custom display name generator for the annotated test class.
public class LibraryEventsServiceIntegrationTest {

  @Autowired
  private LibraryEventsRepository repository;
  @Autowired
  private Gson gson;

//  @Autowired
//  private MockMvc mockMvc;

  @Test
  @SqlGroup({
    @Sql(value = "classpath:reset-data.sql", executionPhase = BEFORE_TEST_METHOD),
    @Sql(value = "classpath:user-data.sql", executionPhase = BEFORE_TEST_METHOD)
  })
  void should_save_library_event() throws IOException {
    //Given
    final File jsonFile = new ClassPathResource("libraryEvent.json").getFile();
    final String libraryEventJson = Files.readString(jsonFile.toPath());
    LibraryEventEntity libraryEvent = gson.fromJson(libraryEventJson, LibraryEventEntity.class);
    int total = 5;

   //When
    // First item added in the DB
    // 4 others are added via "user-data.sql" file in resources
    // So we have to get 5 item in DB
    repository.save(libraryEvent);


    //Then
    // Verify if we have 5 items
    assert repository.count() == total;
    log.info("Total amount of records {} ", repository.count());


  }

}
