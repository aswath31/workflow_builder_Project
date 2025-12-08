package com.workflow_builder.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.workflow_builder.workflow.NodeDefinition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Expects config:
 * expression: JSONPath expression that resolves to boolean or value (truthy)
 * operand: optional compare value
 */
@Component
public class ConditionNodeExecutor implements NodeExecutor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public CompletableFuture<Map<String, Object>> execute(NodeDefinition node, Map<String, Object> input,
            Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> cfg = node.getConfig();
            String expr = Optional.ofNullable(cfg.get("expression")).map(Object::toString).orElse("$");
            Object operand = cfg.get("operand");

            try {
                // Merge context and input so we can access both via JsonPath
                // Convention: context (node outputs) + input (original payload/variables)
                Map<String, Object> data = new HashMap<>();
                if (context != null)
                    data.putAll(context);
                if (input != null)
                    data.putAll(input);

                Object subject = JsonPath.read(mapper.writeValueAsString(data), expr);
                boolean result = false;

                if (operand != null) {
                    // Equality check
                    if (subject == null) {
                        result = (operand == null);
                    } else {
                        // Simple string-based comparison for now to handle mostly all cases
                        // or try strict equals first
                        if (subject.equals(operand)) {
                            result = true;
                        } else {
                            // Fallback to string comparison (e.g. enum vs string, int vs double)
                            result = String.valueOf(subject).equals(String.valueOf(operand));
                        }
                    }
                } else {
                    // Truthy check
                    if (subject instanceof Boolean)
                        result = (Boolean) subject;
                    else if (subject instanceof Number)
                        result = ((Number) subject).doubleValue() != 0;
                    else
                        result = subject != null;
                }

                Map<String, Object> summary = new HashMap<>();
                summary.put("status", "success");
                summary.put("output", Map.of("result", result));
                return summary;
            } catch (Exception e) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("status", "failed");
                summary.put("error", e.getMessage());
                return summary;
            }
        });
    }
}
