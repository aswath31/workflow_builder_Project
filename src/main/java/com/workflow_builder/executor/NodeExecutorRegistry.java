package com.workflow_builder.executor;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NodeExecutorRegistry {

    private final Map<String, NodeExecutor> executors;

    public NodeExecutorRegistry(HttpNodeExecutor http, ConditionNodeExecutor condition,
                                DelayNodeExecutor delay, NotifyNodeExecutor notify) {
        this.executors = Map.of(
                "http", http,
                "condition", condition,
                "delay", delay,
                "notify", notify
        );
    }

    public NodeExecutor get(String type) {
        var executor = executors.get(type.toLowerCase());
        if (executor == null) throw new IllegalArgumentException("Unsupported node type: " + type);
        return executor;
    }
}
