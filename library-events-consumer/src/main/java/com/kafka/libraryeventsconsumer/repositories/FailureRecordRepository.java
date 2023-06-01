package com.kafka.libraryeventsconsumer.repositories;

import com.kafka.libraryeventsconsumer.entity.FailureRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 30/05/2023
 */
public interface FailureRecordRepository extends CrudRepository<FailureRecord, Integer> {
  List<FailureRecord> findAllByStatus(String status);
  FailureRecord findByStatus(String status);
}
