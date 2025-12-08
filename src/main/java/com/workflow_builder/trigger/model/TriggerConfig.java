package com.workflow_builder.trigger.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("trigger_config")
public class TriggerConfig {

    @Id
    private String id;

    private String workflowId;
    private boolean active;
    
    private TriggerType triggerType;

    private String webhookPath; 
    private String cronExpression;

    public enum TriggerType {
        CRON,
        WEBHOOK
    }
}
