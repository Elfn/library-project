package com.kafka.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventTypeEntity;
import com.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 16/04/2023
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsServiceImpl implements LibraryEventsService{

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private LibraryEventsRepository libraryEventsRepository;

  public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
    LibraryEventEntity libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEventEntity.class);
    log.info("libraryEvent : {} ", libraryEvent);

    if(libraryEvent!=null && (libraryEvent.getLibraryEventId()!=null &&  libraryEvent.getLibraryEventId() == 999)){
      throw new RecoverableDataAccessException("Temporary Network Issue");
    }

    switch(libraryEvent.getLibraryEventType()){
      case NEW:
        save(libraryEvent);
        break;
      case UPDATE:
        //validate the libraryevent
        validate(libraryEvent);
        save(libraryEvent);
        break;
      default:
        log.info("Invalid Library Event Type");
    }

  }

  private void validate(LibraryEventEntity libraryEvent) {
    if(libraryEvent.getLibraryEventId()==null){
      throw new IllegalArgumentException("Library Event Id is missing");
    }

    Optional<LibraryEventEntity> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
    if(!libraryEventOptional.isPresent()){
      throw new IllegalArgumentException("Not a valid library Event");
    }
    log.info("Validation is successful for the library Event : {} ", libraryEventOptional.get());
  }

  private void save(LibraryEventEntity libraryEvent) {
    libraryEvent.getBook().setLibraryEvent(libraryEvent);
    libraryEventsRepository.save(libraryEvent);
    log.info("Successfully Persisted the libary Event {} ", libraryEvent);
  }

}
