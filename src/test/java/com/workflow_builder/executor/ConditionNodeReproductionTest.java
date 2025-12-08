package com.workflow_builder.executor;

import com.workflow_builder.workflow.NodeDefinition;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionNodeReproductionTest {

    @Test
    public void testConditionNodeWithContextAndOperand() throws ExecutionException, InterruptedException {
        ConditionNodeExecutor executor = new ConditionNodeExecutor();
        NodeDefinition node = new NodeDefinition();
        node.setType("condition");
        Map<String, Object> config = new HashMap<>();
        // User's expression was checking $.n1.status.
        // With operand support, we extract the value and compare it against operand.
        config.put("expression", "$.n1.status");
        config.put("operand", "SUCCESS");
        node.setConfig(config);

        // Simulate Orchestrator passing n1 output in context
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> n1Output = new HashMap<>();
        n1Output.put("status", "SUCCESS");
        context.put("n1", n1Output);

        Map<String, Object> input = new HashMap<>();
        input.put("payload", Map.of("foo", "bar"));

        Map<String, Object> result = executor.execute(node, input, context).get();
        System.out.println("Result (Operand Match): " + result);

        assertEquals("success", result.get("status"));
        Map<String, Object> output = (Map<String, Object>) result.get("output");
        assertEquals(true, output.get("result"));
    }

    @Test
    public void testConditionNodeWithContextAndOperandMismatch() throws ExecutionException, InterruptedException {
        ConditionNodeExecutor executor = new ConditionNodeExecutor();
        NodeDefinition node = new NodeDefinition();
        node.setType("condition");
        Map<String, Object> config = new HashMap<>();
        config.put("expression", "$.n1.status");
        config.put("operand", "SUCCESS");
        node.setConfig(config);

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> n1Output = new HashMap<>();
        n1Output.put("status", "FAILED");
        context.put("n1", n1Output);

        Map<String, Object> input = new HashMap<>();

        Map<String, Object> result = executor.execute(node, input, context).get();
        System.out.println("Result (Operand Mismatch): " + result);

        assertEquals("success", result.get("status")); // The node execution succeeded, but the condition result is
                                                       // false
        Map<String, Object> output = (Map<String, Object>) result.get("output");
        assertEquals(false, output.get("result"));
    }

    @Test
    public void testOriginalBooleanBehavior() throws ExecutionException, InterruptedException {
        // Existing behavior: expression evaluates to boolean directly
        ConditionNodeExecutor executor = new ConditionNodeExecutor();
        NodeDefinition node = new NodeDefinition();
        node.setType("condition");
        Map<String, Object> config = new HashMap<>();
        // Use an expression that returns boolean directly from input
        config.put("expression", "$.payload.isValid");
        node.setConfig(config);

        Map<String, Object> input = new HashMap<>();
        input.put("payload", Map.of("isValid", true));

        Map<String, Object> result = executor.execute(node, input, new HashMap<>()).get();
        System.out.println("Result (Boolean Expr): " + result);

        assertEquals("success", result.get("status"));
        Map<String, Object> output = (Map<String, Object>) result.get("output");
        assertEquals(true, output.get("result"));
    }
}
