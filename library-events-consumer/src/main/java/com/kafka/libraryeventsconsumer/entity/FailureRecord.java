package com.kafka.libraryeventsconsumer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @PROJECT library-events-consumer
 * @Author Elimane on 30/05/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class FailureRecord {

    @Id
    @GeneratedValue
    private Integer id;
    private String fromTopic;
    private Integer key;
    private String errorRecord;
    private Integer partition;
    private Long offsetValue;
    private String exception;
    private String status;

}
