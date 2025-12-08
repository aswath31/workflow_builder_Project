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
            String rawExpr = Optional.ofNullable(cfg.get("expression")).map(Object::toString).orElse("$");
            Object cfgOperand = cfg.get("operand");

            String expr = rawExpr;
            Object finalOperand = cfgOperand;

            if (finalOperand == null && rawExpr.contains("==")) {
                String[] parts = rawExpr.split("==", 2);
                expr = parts[0].trim();
                String opStr = parts[1].trim();

                if ((opStr.startsWith("'") && opStr.endsWith("'"))
                        || (opStr.startsWith("\"") && opStr.endsWith("\""))) {
                    opStr = opStr.substring(1, opStr.length() - 1);
                }
                finalOperand = opStr;
            }

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

                if (finalOperand != null) {
                    // Equality check
                    if (subject == null) {
                        result = (finalOperand == null);
                    } else {
                        // Simple string-based comparison for now to handle mostly all cases
                        // or try strict equals first
                        if (subject.equals(finalOperand)) {
                            result = true;
                        } else {
                            // Fallback to string comparison (e.g. enum vs string, int vs double)
                            result = String.valueOf(subject).equals(String.valueOf(finalOperand));
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
