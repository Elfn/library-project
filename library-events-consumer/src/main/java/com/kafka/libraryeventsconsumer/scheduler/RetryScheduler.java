package com.kafka.libraryeventsconsumer.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.libraryeventsconsumer.config.RecoverySignals;
import com.kafka.libraryeventsconsumer.entity.FailureRecord;
import com.kafka.libraryeventsconsumer.repositories.FailureRecordRepository;
import com.kafka.libraryeventsconsumer.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 01/06/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

  private final FailureRecordRepository failureRecordRepository;
  private final LibraryEventsService libraryEventsService;

  @Scheduled(fixedRate = 10000)
//  @Scheduled(cron = "*/10 * * * * ?") // cron expression
  public void retryFailedRecord() {
    failureRecordRepository.findAllByStatus(RecoverySignals.RETRY).forEach(failureRecord -> {

        log.info("Retrying Failed Records Started!");
        try {
          log.info("Retrying Failed Records : {} ", failureRecord);
          var record = buildConsumerRecord(failureRecord);
          libraryEventsService.processLibraryEvent(record);
          failureRecord.setStatus(RecoverySignals.SUCCESS);
          failureRecordRepository.save(failureRecord);
        } catch (JsonProcessingException e) {
          log.error("Exception in retryFailedRecords : {}", e.getCause().getMessage(), e);
        }

    });
    log.info("Retrying Failed Records Completed!");

  }

  private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
    // Création d'un ConsumerRecord pour des tests
    var record = new ConsumerRecord<>(failureRecord.getFromTopic(), failureRecord.getPartition(), failureRecord.getOffsetValue(), failureRecord.getKey(), failureRecord.getErrorRecord());
    log.info("Built from scheduler record : {}", record);
    return record;
  }

}
