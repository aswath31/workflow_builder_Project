package com.workflow_builder.executor;

import com.workflow_builder.workflow.NodeDefinition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Schedules resume after given seconds via ScheduledExecutorService.
 * Returns status queued (the orchestrator should wait/resume when delay completes).
 */
@Component
public class DelayNodeExecutor implements NodeExecutor {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public CompletableFuture<Map<String, Object>> execute(NodeDefinition node, Map<String, Object> input, Map<String,Object> context) {

        Map<String,Object> cfg = node.getConfig();
        int seconds = Optional.ofNullable(cfg.get("seconds")).map(Object::toString).map(Integer::valueOf).orElse(5);

        CompletableFuture<Map<String,Object>> future = new CompletableFuture<>();

        scheduler.schedule(() -> {
            Map<String,Object> summary = new HashMap<>();
            summary.put("status","success");
            summary.put("output", Map.of("delayedSeconds", seconds));
            future.complete(summary);
        }, seconds, TimeUnit.SECONDS);

        return future;
    }
}
