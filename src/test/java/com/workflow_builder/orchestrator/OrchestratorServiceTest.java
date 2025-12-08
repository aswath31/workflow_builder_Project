package com.workflow_builder.orchestrator;

import com.workflow_builder.executor.NodeExecutor;
import com.workflow_builder.executor.NodeExecutorRegistry;
import com.workflow_builder.log.model.LogEntry;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.model.RunNode;
import com.workflow_builder.log.repo.LogRepository;
import com.workflow_builder.run.repo.RunRepository;
import com.workflow_builder.workflow.NodeDefinition;

import com.workflow_builder.workflow.WorkflowRepository;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrchestratorServiceTest {

    @Test
    void testExecuteNodeRecursive_successFlow() throws Exception {

        // ------------------------------
        // Mock dependencies
        // ------------------------------
        RunRepository runRepo = mock(RunRepository.class);
        WorkflowRepository wfRepo = mock(WorkflowRepository.class);
        NodeExecutorRegistry registry = mock(NodeExecutorRegistry.class);
        LogRepository logRepo = mock(LogRepository.class);

        OrchestratorService service =
                new OrchestratorService(runRepo, wfRepo, registry, logRepo);

        when(runRepo.save(any(Run.class))).thenAnswer(inv -> inv.getArgument(0));

        // ------------------------------
        // Mock Workflow + NodeDefinition
        // ------------------------------
        NodeDefinition startNode = mock(NodeDefinition.class);
        when(startNode.getId()).thenReturn("start");
        when(startNode.getType()).thenReturn("task");
        when(startNode.getNextRaw()).thenReturn(List.of("next1"));

        NodeDefinition nextNode = mock(NodeDefinition.class);
        when(nextNode.getId()).thenReturn("next1");
        when(nextNode.getType()).thenReturn("task");
        when(nextNode.getNextRaw()).thenReturn(null);

        Map<String, NodeDefinition> nodeMap = new HashMap<>();
        nodeMap.put("start", startNode);
        nodeMap.put("next1", nextNode);

        // ------------------------------
        // Mock Run
        // ------------------------------
        Run run = new Run();
        run.setId("RUN-1");
        run.setWorkflowId("WF-1");
        run.setNodes(new ArrayList<>());

        // ------------------------------
        // Mock Node Executor
        // ------------------------------
        NodeExecutor executor = mock(NodeExecutor.class);
        when(registry.get("task")).thenReturn(executor);

        when(executor.execute(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(
                        Map.of("status", "success")
                ));

        // ------------------------------
        // Invoke private method executeNodeRecursive
        // ------------------------------
        var method = OrchestratorService.class.getDeclaredMethod(
                "executeNodeRecursive",
                Run.class,
                Map.class,
                String.class,
                Map.class,
                Map.class
        );

        method.setAccessible(true);

        boolean result = (boolean) method.invoke(
                service,
                run,
                nodeMap,
                "start",
                new HashMap<>(),
                new HashMap<>()
        );

        // ------------------------------
        // Assertions
        // ------------------------------
        assertTrue(result, "Execution should succeed");
        assertEquals(2, run.getNodes().size(), "Both nodes should be executed");

        RunNode rn1 = run.getNodes().get(0);
        assertEquals("start", rn1.getNodeId());
        assertEquals("success", rn1.getStatus());

        RunNode rn2 = run.getNodes().get(1);
        assertEquals("next1", rn2.getNodeId());
        assertEquals("success", rn2.getStatus());

        // Executor called twice (start + next1)
        verify(executor, times(2)).execute(any(), any(), any());

        // Logging should happen
        verify(logRepo, atLeastOnce()).save(any(LogEntry.class));
    }
}
