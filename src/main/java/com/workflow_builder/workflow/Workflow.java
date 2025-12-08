package com.workflow_builder.workflow;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("workflows")
public class Workflow {

    @Id
    private String id;

    private String ownerId; 
    private String name;
    private String description;

    private String state; // DRAFT or PUBLISHED
    private Integer version;

    private List<NodeDefinition> nodes;
    private List<TriggerDefinition> triggers;

    private Instant createdAt;
    private Instant updatedAt;
}
