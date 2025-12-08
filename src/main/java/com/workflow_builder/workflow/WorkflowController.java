package com.workflow_builder.workflow;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    // Create DRAFT workflow
    @PostMapping
    public Workflow create(
            Authentication auth,
            @RequestBody Workflow request) {

        String ownerId = auth.getName();
        return workflowService.createWorkflow(ownerId, request);
    }

    // Update DRAFT workflow
    @PutMapping("/{id}")
    public Workflow update(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Workflow request) {

        String ownerId = auth.getName();
        return workflowService.updateWorkflow(ownerId, id, request);
    }
    @PostMapping("/{id}/start")
public WorkflowExecutionResponse start(
        Authentication auth,
        @PathVariable String id,
        @RequestBody Map<String, Object> input) {

    String userId = auth.getName();
    return workflowService.startWorkflow(userId, id, input);
}

    // Publish workflow
    @PostMapping("/{id}/publish")
    public Workflow publish(
            Authentication auth,
            @PathVariable String id) {

        String ownerId = auth.getName();
        return workflowService.publishWorkflow(ownerId, id);
    }

    // Get workflow details
    @GetMapping("/{id}")
    public Workflow get(@PathVariable String id) {
        return workflowService.getWorkflow(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
    }
}
