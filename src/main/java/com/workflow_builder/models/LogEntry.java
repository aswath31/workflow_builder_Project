package com.workflow_builder.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("logs")
@Data
public class LogEntry {
    @Id private String id;
    private String runId;
    private String nodeId;
    private String level; // INFO/ERROR
    private String message;
    private Map<String,Object> details;
    private Instant timestamp;
}
