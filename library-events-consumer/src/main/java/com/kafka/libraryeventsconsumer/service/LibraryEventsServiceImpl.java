package com.kafka.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import com.kafka.libraryeventsconsumer.entity.LibraryEventTypeEntity;
import com.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

 private final ObjectMapper objectMapper;
 private final LibraryEventsRepository repository;

  @Override
  public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
    LibraryEventEntity libraryEvent =  objectMapper.readValue(consumerRecord.value(), LibraryEventEntity.class);
    log.info("LibraryEvent : {}", libraryEvent);

    if(libraryEvent != null && libraryEvent.getLibraryEventId() == 999){
      throw new RecoverableDataAccessException("Temporary network issue");
    }

    switch(libraryEvent.getLibraryEventType()){
      case NEW:
        // Save operation
        save(libraryEvent);
        break;
      case UPDATE:
        // Update operation
        validate(libraryEvent);
        save(libraryEvent);
        break;
      default:
        log.error("Invalid Library Event Type");
    }
  }

  private void save(LibraryEventEntity libraryEvent) {
    libraryEvent.getBook().setLibraryEvent(libraryEvent);
    repository.save(libraryEvent);
    log.info("Successfully persisted the libraryEvent (Id:{}, Type:{})", libraryEvent.getLibraryEventId(), libraryEvent.getLibraryEventType());
  }

  private void validate(LibraryEventEntity libraryEvent) {
     if(libraryEvent.getLibraryEventId() == null){
       throw new IllegalArgumentException("Library event Id is missing");
     }
     Optional<LibraryEventEntity> optionalLibraryEvent =  repository.findById(libraryEvent.getLibraryEventId());
     if(!optionalLibraryEvent.isPresent()){
       throw new IllegalArgumentException("Library event not valid");
     }

     log.info("Validation is successfull for the library event : {} ", optionalLibraryEvent.get());
  }


}
