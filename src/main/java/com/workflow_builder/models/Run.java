package com.workflow_builder.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document("runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Run {
    @Id private String id;
    private String workflowId;
    private String ownerId;
    private String status; // queued|running|failed|completed
    private Instant startTime;
    private Instant endTime;
    private Map<String,Object> input;
    private Map<String,Object> outputSummary;
    private List<RunNode> nodes;
    private int attempts;
}
