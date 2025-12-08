package com.workflow_builder.orchestrator;

import com.workflow_builder.executor.NodeExecutor;
import com.workflow_builder.executor.NodeExecutorRegistry;
import com.workflow_builder.log.model.LogEntry;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.model.RunNode;
import com.workflow_builder.log.repo.LogRepository;
import com.workflow_builder.run.repo.RunRepository;
import com.workflow_builder.workflow.NodeDefinition;
import com.workflow_builder.workflow.Workflow;
import com.workflow_builder.workflow.WorkflowRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final RunRepository runRepository;
    private final WorkflowRepository workflowRepository;
    private final NodeExecutorRegistry registry;
    private final LogRepository logRepository;

    private final int maxRetries = 3;
    private final long baseBackoffMs = 1000L;

    @Async
    public void processRun(String runId) {
        Run run = runRepository.findById(runId).orElseThrow();
        Workflow wf = workflowRepository.findById(run.getWorkflowId()).orElseThrow();

        run.setStatus("running");
        run.setStartTime(Instant.now());
        runRepository.save(run);

        // Build node lookup map
        Map<String, NodeDefinition> nodeMap = new HashMap<>();
        for (var n : wf.getNodes())
            nodeMap.put(n.getId(), n);

        // Identify entry nodes
        Set<String> referenced = new HashSet<>();

        wf.getNodes().forEach(n -> {
            Object nextObj = getNextRaw(n);

            if (nextObj instanceof Collection<?>) {
                referenced.addAll((Collection<String>) nextObj);
            } else if (nextObj instanceof String) {
                referenced.add((String) nextObj);
            }

            if (n.getOnTrue() != null)
                referenced.add(n.getOnTrue());
            if (n.getOnFalse() != null)
                referenced.add(n.getOnFalse());
        });

        List<String> entryNodeIds = new ArrayList<>();
        for (var n : wf.getNodes()) {
            if (!referenced.contains(n.getId())) {
                entryNodeIds.add(n.getId());
            }
        }

        if (entryNodeIds.isEmpty() && !wf.getNodes().isEmpty()) {
            entryNodeIds.add(wf.getNodes().get(0).getId());
        }

        Map<String, Object> context = new HashMap<>();
        run.setNodes(new ArrayList<>());
        runRepository.save(run);

        for (String entryId : entryNodeIds) {
            boolean ok = executeNodeRecursive(run, nodeMap, entryId, run.getInput(), context);
            if (!ok) {
                run.setStatus("failed");
                run.setEndTime(Instant.now());
                runRepository.save(run);
                return;
            }
        }

        run.setStatus("completed");
        run.setEndTime(Instant.now());
        runRepository.save(run);
    }

    private boolean executeNodeRecursive(
            Run run,
            Map<String, NodeDefinition> nodes,
            String nodeId,
            Map<String, Object> input,
            Map<String, Object> context) {
        NodeDefinition node = nodes.get(nodeId);
        if (node == null) {
            log(run.getId(), nodeId, "ERROR", "Node not found: " + nodeId, null);
            return false;
        }

        RunNode rn = new RunNode();
        rn.setNodeId(nodeId);
        rn.setStatus("running");
        rn.setStartTime(Instant.now());
        run.getNodes().add(rn);
        runRepository.save(run);

        int attempt = 0;
        Map<String, Object> resultSummary = null;

        while (attempt <= maxRetries) {
            try {
                NodeExecutor executor = registry.get(node.getType());
                CompletableFuture<Map<String, Object>> fut = executor.execute(node, input, context);

                resultSummary = fut.get();
                String status = (String) resultSummary.getOrDefault("status", "failed");

                if ("success".equalsIgnoreCase(status)) {
                    rn.setStatus("success");
                    rn.setEndTime(Instant.now());
                    rn.setSummary(resultSummary);
                    runRepository.save(run);
                    log(run.getId(), nodeId, "INFO", "Node succeeded", resultSummary);

                    // Propagate output to context for subsequent nodes
                    if (resultSummary.containsKey("output")) {
                        context.put(nodeId, resultSummary.get("output"));
                    }

                    break;
                } else {
                    attempt++;
                    if (attempt > maxRetries) {
                        rn.setStatus("failed");
                        rn.setEndTime(Instant.now());
                        rn.setSummary(resultSummary);
                        runRepository.save(run);

                        log(run.getId(), nodeId, "ERROR",
                                "Max retries exceeded", resultSummary);
                        return false;
                    }
                    long backoff = computeBackoff(attempt);
                    log(run.getId(), nodeId, "INFO",
                            "Retry " + attempt + ", backoff " + backoff + "ms", null);
                    Thread.sleep(backoff);
                }

            } catch (Exception e) {
                attempt++;

                if (attempt > maxRetries) {
                    rn.setStatus("failed");
                    rn.setEndTime(Instant.now());
                    rn.setSummary(Map.of(
                            "status", "failed",
                            "error", e.getMessage()));

                    runRepository.save(run);

                    log(run.getId(), nodeId, "ERROR",
                            "Exception: " + e.getMessage(), null);

                    return false;
                }

                long backoff = computeBackoff(attempt);
                log(run.getId(), nodeId, "INFO",
                        "Exception, retry " + attempt + ", backoff " + backoff + "ms",
                        Map.of("error", e.getMessage()));

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ignored) {
                }
            }
        }

        // Conditional node
        if ("condition".equalsIgnoreCase(node.getType())) {
            Object output = resultSummary != null ? resultSummary.get("output") : null;
            boolean truthy = false;

            if (output instanceof Map) {
                truthy = Boolean.TRUE.equals(((Map<?, ?>) output).get("result"));
            } else if (output instanceof Boolean) {
                truthy = (Boolean) output;
            }

            String nextId = truthy ? node.getOnTrue() : node.getOnFalse();
            if (nextId != null) {
                return executeNodeRecursive(run, nodes, nextId, input, context);
            }
            return true;
        }

        // Generic next / next[] handling
        List<String> nexts = new ArrayList<>();
        Object nextObj = getNextRaw(node);

        if (nextObj instanceof Collection<?>) {
            nexts.addAll((Collection<String>) nextObj);
        } else if (nextObj instanceof String) {
            nexts.add((String) nextObj);
        }

        for (String nextId : nexts) {
            boolean ok = executeNodeRecursive(run, nodes, nextId, input, context);
            if (!ok)
                return false;
        }

        return true;
    }

    private Object getNextRaw(NodeDefinition node) {
        // You MUST add a method in NodeDefinition like:
        // public Object getNextRaw() { return next; }
        return node.getNextRaw();
    }

    private long computeBackoff(int attempt) {
        long base = baseBackoffMs * (1L << (attempt - 1));
        return base + ThreadLocalRandom.current().nextLong(0, 200L);
    }

    private void log(String runId, String nodeId, String level, String message, Map<String, Object> details) {
        LogEntry le = new LogEntry();
        le.setRunId(runId);
        le.setNodeId(nodeId);
        le.setLevel(level);
        le.setMessage(message);
        le.setDetails(details);
        le.setTimestamp(Instant.now());
        logRepository.save(le);
    }
}
