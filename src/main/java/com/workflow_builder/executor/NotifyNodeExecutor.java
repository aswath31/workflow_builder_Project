package com.workflow_builder.executor;

import com.workflow_builder.workflow.NodeDefinition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Simple notify node placeholder. In MVP this simulates sending a notification.
 * Config may include: provider (email|slack), to, template, subject, body.
 */
@Component
public class NotifyNodeExecutor implements NodeExecutor {

    @Override
    public CompletableFuture<Map<String, Object>> execute(NodeDefinition node, Map<String, Object> input, Map<String,Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String,Object> cfg = node.getConfig();
            // TODO: integrate with SMTP or Slack API
            Map<String,Object> summary = new HashMap<>();
            summary.put("status", "success");
            summary.put("output", Map.of("notified", true, "provider", cfg.getOrDefault("provider","stub")));
            return summary;
        });
    }
}
