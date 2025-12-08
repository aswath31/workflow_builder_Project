package com.workflow_builder.workflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.workflow_builder.log.service.LogService;

import com.workflow_builder.run.model.Run;  // ✅ CORRECT
import com.workflow_builder.orchestrator.OrchestratorService;

import com.workflow_builder.run.repo.RunRepository;
import com.workflow_builder.run.service.RunService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final RunService runService;
    private final LogService logService;
    private final OrchestratorService orchestratorService;

    public Workflow createWorkflow(String ownerId, Workflow request) {

        request.setId(null);
        request.setOwnerId(ownerId);
        request.setState("DRAFT");
        request.setVersion(1);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        return workflowRepository.save(request);
    }

    public Workflow updateWorkflow(String ownerId, String id, Workflow request) {

        Workflow existing = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!existing.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if ("PUBLISHED".equals(existing.getState())) {
            throw new RuntimeException("Published workflows cannot be modified");
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setNodes(request.getNodes());
        existing.setTriggers(request.getTriggers());
        existing.setUpdatedAt(Instant.now());

        return workflowRepository.save(existing);
    }

    public Workflow publishWorkflow(String ownerId, String id) {

        Workflow wf = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!wf.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        wf.setState("PUBLISHED");
        wf.setVersion(wf.getVersion() + 1);
        wf.setUpdatedAt(Instant.now());

        return workflowRepository.save(wf);
    }

    public Optional<Workflow> getWorkflow(String id) {
        return workflowRepository.findById(id);
    }

    public WorkflowExecutionResponse startWorkflow(String ownerId, String id, Map<String, Object> input) {

    Workflow wf = workflowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));

    if (!wf.getOwnerId().equals(ownerId)) {
        throw new RuntimeException("Unauthorized");
    }

    if (!"PUBLISHED".equals(wf.getState())) {
        throw new RuntimeException("Only published workflows can be started");
    }

    // ✅ CREATE RUN ENTRY IN DB
    Run run = runService.createQueuedRun(id, ownerId, input);

    // ✅ TRIGGER ORCHESTRATOR
    orchestratorService.processRun(run.getId());

    // ✅ LOG ENTRY
    logService.append(
            run.getId(),
            "START",
            "INFO",
            "Workflow execution started",
            input
    );

    return new WorkflowExecutionResponse(
            run.getId(),
            "STARTED",
            input
    );
}


}



