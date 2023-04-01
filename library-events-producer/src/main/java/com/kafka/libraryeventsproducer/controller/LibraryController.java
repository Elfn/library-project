package com.kafka.libraryeventsproducer.controller;

import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 26/03/2023
 */
@RestController
public class LibraryController {

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<LibraryEvent>  postLibraryEvent(@RequestBody LibraryEvent libraryEvent){

    // Invoke Kafka producer

    return  ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);

  }

}
