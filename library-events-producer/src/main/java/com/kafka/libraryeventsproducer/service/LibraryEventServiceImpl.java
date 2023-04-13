package com.kafka.libraryeventsproducer.service;

import com.kafka.libraryeventsproducer.domain.Book;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.domain.LibraryEventType;
import com.kafka.libraryeventsproducer.dto.LibraryEventRequest;
import com.kafka.libraryeventsproducer.dto.LibraryEventResponse;
import org.springframework.stereotype.Service;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
@Service
public class LibraryEventServiceImpl implements LibraryEventService {

  @Override
  public LibraryEvent postLibraryEvent(LibraryEventRequest request) {
    request.setLibraryEventType(LibraryEventType.NEW);
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(request.getLibraryEventId()).libraryEventType(request.getLibraryEventType()).book(request.getBook()).build();
    return libraryEvent;
  }

  @Override
  public LibraryEvent putLibraryEvent(LibraryEventRequest request) {
    request.setLibraryEventType(LibraryEventType.UPDATE);
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(request.getLibraryEventId()).libraryEventType(request.getLibraryEventType()).book(request.getBook()).build();
    return libraryEvent;
  }
}
