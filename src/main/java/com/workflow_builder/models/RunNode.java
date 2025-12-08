package com.workflow_builder.models;

import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class RunNode {
    private String nodeId;
    private String status; // pending|running|success|failed
    private Instant startTime;
    private Instant endTime;
    private Map<String,Object> summary;
}
