package com.kafka.libraryeventsconsumer.jpa;

import com.kafka.libraryeventsconsumer.entity.LibraryEventEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 16/04/2023
 */
public interface LibraryEventsRepository extends CrudRepository<LibraryEventEntity, Integer> {
}
