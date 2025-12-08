package com.workflow_builder.log.service;

import com.workflow_builder.log.model.LogEntry;
import com.workflow_builder.log.repo.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogEntry append(String runId, String nodeId, String level, String message, Map<String,Object> details) {
        LogEntry e = LogEntry.builder()
                .runId(runId)
                .nodeId(nodeId)
                .level(level)
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .build();
        return logRepository.save(e);
    }

    public List<LogEntry> getLogsForRun(String runId) {
        return logRepository.findByRunIdOrderByTimestampAsc(runId);
    }
}
