package com.workflow_builder.workflow;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDefinition {

    private String type;        // "WEBHOOK" or "CRON"
    private String webhookId;   // only for webhook triggers
    private String cron;        // only for cron triggers
    private String secret;      // for webhook trigger validation
}
