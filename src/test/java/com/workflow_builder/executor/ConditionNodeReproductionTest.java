package com.workflow_builder.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow_builder.workflow.NodeDefinition;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionNodeReproductionTest {

    @Test
    public void testConditionNodeWithSplitSyntax() throws ExecutionException, InterruptedException {

        ConditionNodeExecutor executor = new ConditionNodeExecutor();
        NodeDefinition node = new NodeDefinition();
        node.setType("condition");
        Map<String, Object> config = new HashMap<>();
        config.put("expression", "$.n1.status == 'SUCCESS'");
        node.setConfig(config);

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> n1Output = new HashMap<>();
        n1Output.put("status", "SUCCESS");
        context.put("n1", n1Output);

        Map<String, Object> input = new HashMap<>();

        Map<String, Object> result = executor.execute(node, input, context).get();
        System.out.println("Result (Split Syntax Match): " + result);

        assertEquals("success", result.get("status"));
        Map<String, Object> output = (Map<String, Object>) result.get("output");
        assertEquals(true, output.get("result"));
    }

    @Test
    public void testConditionNodeWithSplitSyntaxMismatch() throws ExecutionException, InterruptedException {
        ConditionNodeExecutor executor = new ConditionNodeExecutor();
        NodeDefinition node = new NodeDefinition();
        node.setType("condition");
        Map<String, Object> config = new HashMap<>();
        config.put("expression", "$.n1.status == 'SUCCESS'");
        node.setConfig(config);

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> n1Output = new HashMap<>();
        n1Output.put("status", "FAILED");
        context.put("n1", n1Output);

        Map<String, Object> input = new HashMap<>();

        Map<String, Object> result = executor.execute(node, input, context).get();
        System.out.println("Result (Split Syntax Mismatch): " + result);

        assertEquals("success", result.get("status"));
        Map<String, Object> output = (Map<String, Object>) result.get("output");
        assertEquals(false, output.get("result"));
    }
}
