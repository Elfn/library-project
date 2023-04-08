package com.kafka.libraryeventsproducer.service;

import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.dto.LibraryEventRequest;
import com.kafka.libraryeventsproducer.dto.LibraryEventResponse;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
public interface LibraryEventService {
  public LibraryEvent postLibraryEvent(LibraryEventRequest request);
}
