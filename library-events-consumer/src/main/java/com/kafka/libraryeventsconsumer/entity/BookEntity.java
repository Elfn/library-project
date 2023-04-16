package com.kafka.libraryeventsconsumer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @PROJECT library-events-producer
 * @Author Elimane on 26/03/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class BookEntity {

  @Id
  private Integer bookId;
  private String bookName;
  private String bookAuthor;
  @OneToOne
  @JoinColumn(name = "libraryEventId")
  private LibraryEventEntity libraryEvent;

}
