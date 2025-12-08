package com.workflow_builder.log.repo;

import com.workflow_builder.log.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LogRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByRunIdOrderByTimestampAsc(String runId);
}
