package com.workflow_builder.executor;

//import com.workflow_builder.models.RunNode;
import com.workflow_builder.workflow.NodeDefinition;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Executes a single node. Returns a future with execution summary map.
 */
public interface NodeExecutor {
    /**
     * Execute node with given input and context
     * @param node node definition
     * @param input input payload (run-level)
     * @param context runtime context (e.g., variables)
     * @return CompletableFuture of summary map containing keys: status, output, error (optional)
     */
    CompletableFuture<Map<String,Object>> execute(NodeDefinition node, Map<String,Object> input, Map<String,Object> context);
}
