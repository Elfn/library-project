package com.kafka.libraryeventsproducer.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.libraryeventsproducer.controller.LibraryEventController;
import com.kafka.libraryeventsproducer.domain.Book;
import com.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.kafka.libraryeventsproducer.domain.LibraryEventType;
import com.kafka.libraryeventsproducer.dto.LibraryEventRequest;
import com.kafka.libraryeventsproducer.dto.LibraryEventResponse;
import com.kafka.libraryeventsproducer.producer.LibraryEventProducerWithSend;
import com.kafka.libraryeventsproducer.service.LibraryEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 09/04/2023
 */
@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {

  @Autowired
  MockMvc mockMvc;

  // To simulate external dependency of LibraryController
  @MockBean
  LibraryEventProducerWithSend producer;
  // To simulate external dependency of LibraryController
  @MockBean
  LibraryEventService service;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void postLibraryEvent() throws Exception {
    //Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
    LibraryEventRequest request = LibraryEventRequest.builder().libraryEventType(libraryEvent.getLibraryEventType()).book(libraryEvent.getBook()).build();
    String json = objectMapper.writeValueAsString(request);
    when(producer.sendLibraryEventAsynchronousWayReturnCompletableFuture(isA(LibraryEvent.class))).thenReturn(null);
    when(service.postLibraryEvent(request)).thenReturn(libraryEvent);

    //When
    mockMvc.perform(post("/v1/libraryevent")
      .content(json)
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    //Then
  }

  @Test
  void postLibraryEvent_4xx() throws Exception {
    // Given
    Book book = Book.builder().bookId(null).bookAuthor(null).bookName(null).build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
    LibraryEventRequest request = LibraryEventRequest.builder().libraryEventType(libraryEvent.getLibraryEventType()).book(libraryEvent.getBook()).build();
    String json = objectMapper.writeValueAsString(request);
    when(producer.sendLibraryEventAsynchronousWayReturnCompletableFuture(isA(LibraryEvent.class))).thenReturn(null);
    when(service.postLibraryEvent(request)).thenReturn(libraryEvent);

    // Expected
    String expectedErrorMessage = "book.bookAuthor - must not be blank, book.bookId - must not be null, book.bookName - must not be blank";
    mockMvc.perform(post("/v1/libraryevent")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError())
      .andExpect(content().string(expectedErrorMessage)); // Expect all "book" fields to null

  }

  @Test
  void putLibraryEvent() throws Exception {
    //Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(999).libraryEventType(LibraryEventType.UPDATE).book(book).build();
   // LibraryEvent libraryEventNull = LibraryEvent.builder().libraryEventId(null).libraryEventType(LibraryEventType.UPDATE).book(book).build();
    LibraryEventRequest request = LibraryEventRequest.builder().libraryEventId(libraryEvent.getLibraryEventId()).libraryEventType(libraryEvent.getLibraryEventType()).book(libraryEvent.getBook()).build();
    String json = objectMapper.writeValueAsString(request);

    //When
    when(producer.sendLibraryEventAsynchronousWayReturnCompletableFuture(isA(LibraryEvent.class))).thenReturn(any());
    when(service.putLibraryEvent(request)).thenReturn(libraryEvent);

    //Then
    mockMvc.perform(put("/v1/libraryevent")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

  }

  @Test
  void updateLibraryEvent_withNullLibraryEventId() throws Exception {
    //Given
    Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka Using Spring Boot").build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).libraryEventType(LibraryEventType.UPDATE).book(book).build();
    LibraryEventRequest request = LibraryEventRequest.builder().libraryEventId(libraryEvent.getLibraryEventId()).libraryEventType(libraryEvent.getLibraryEventType()).book(libraryEvent.getBook()).build();
    String json = objectMapper.writeValueAsString(request);

    //When
    when(producer.sendLibraryEventAsynchronousWayReturnCompletableFuture(isA(LibraryEvent.class))).thenReturn(any());
    when(service.putLibraryEvent(request)).thenReturn(libraryEvent);

    //Then
    mockMvc.perform(put("/v1/libraryevent")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());

  }

  @Test
  void putLibraryEvent_4xx() throws Exception {
    // Given
    Book book = Book.builder().bookId(null).bookAuthor(null).bookName(null).build();
    LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
    LibraryEventRequest request = LibraryEventRequest.builder().libraryEventType(libraryEvent.getLibraryEventType()).book(libraryEvent.getBook()).build();
    String json = objectMapper.writeValueAsString(request);
    when(producer.sendLibraryEventAsynchronousWayReturnCompletableFuture(isA(LibraryEvent.class))).thenReturn(null);
    when(service.putLibraryEvent(request)).thenReturn(libraryEvent);

    // Expected
    String expectedErrorMessage = "book.bookAuthor - must not be blank, book.bookId - must not be null, book.bookName - must not be blank";
    mockMvc.perform(post("/v1/libraryevent")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError())
      .andExpect(content().string(expectedErrorMessage)); // Expect all "book" fields to null

  }

}
