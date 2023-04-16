package com.kafka.libraryeventsconsumer.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;


/**
 * @PROJECT library-events-producer
 * @Author Elimane on 26/03/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEventEntity {

  @Id
  @GeneratedValue
  private Integer libraryEventId;
  @Enumerated(EnumType.STRING)
  private LibraryEventTypeEntity libraryEventType;
  @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
  @ToString.Exclude // To avoid circular dependency
  private BookEntity book;

}
