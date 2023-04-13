package com.kafka.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.dto.LibraryEventRequest;
import com.kafka.libraryeventsproducer.dto.LibraryEventResponse;
import com.kafka.libraryeventsproducer.producer.LibraryEventProducerWithSend;
import com.kafka.libraryeventsproducer.service.LibraryEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 26/03/2023
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class LibraryEventController {

  //private final LibraryEventProducerWithSendDefault producerSendDefault;
  private final LibraryEventProducerWithSend producerSend;
  private final LibraryEventService service;

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<LibraryEventResponse>  postLibraryEvent(@RequestBody @Valid LibraryEventRequest libraryEventRequest) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

    LibraryEvent libraryEvent = service.postLibraryEvent(libraryEventRequest);
    LibraryEventResponse response = getLibraryEventResponse(libraryEvent);

    // Invoke Kafka producer
    log.info("Before send");

    // Send async
    // producerSend.sendLibraryEventAsynchronousWay(libraryEvent);

    // Send sync
   producerSend.sendLibraryEventAsynchronousWay(libraryEvent);
//    SendResult<Integer, String> sendResult = producerSend.sendLibraryEventSynchronousWay(libraryEvent);
//    log.info("Result is : {}", sendResult);
    log.info("After sent");



    return  ResponseEntity.status(HttpStatus.CREATED).body(response);

  }

  @PutMapping("/v1/libraryevent")
  public ResponseEntity<?>  putLibraryEvent(@RequestBody @Valid LibraryEventRequest libraryEventRequest) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {


    LibraryEvent libraryEvent = service.putLibraryEvent(libraryEventRequest);

    if(libraryEvent.getLibraryEventId() == null){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventId");
    }
    LibraryEventResponse response = getLibraryEventResponse(libraryEvent);
    // Invoke Kafka producer
    log.info("Before send");
    // Send sync
    producerSend.sendLibraryEventAsynchronousWay(libraryEvent);
    log.info("After sent");

    return  ResponseEntity.status(HttpStatus.OK).body(response);

  }


  private LibraryEventResponse getLibraryEventResponse(LibraryEvent libraryEvent) {
    return LibraryEventResponse.builder()
     // .libraryEventId(libraryEvent.getLibraryEventId())
      .libraryEventType(libraryEvent.getLibraryEventType())
      .book(libraryEvent.getBook())
      .build();
  }

}
