package com.workflow_builder.trigger.model;

import lombok.Data;

@Data
public class WebhookPayload {
    private String event;
    private String data;
}