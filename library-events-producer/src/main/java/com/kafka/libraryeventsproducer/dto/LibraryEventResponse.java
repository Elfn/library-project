package com.kafka.libraryeventsproducer.dto;

import com.kafka.libraryeventsproducer.domain.Book;
import com.kafka.libraryeventsproducer.domain.LibraryEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 08/04/2023
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryEventResponse {

  private Integer libraryEventId;
  private LibraryEventType libraryEventType;
  private Book book;

}
