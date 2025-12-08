package com.workflow_builder.run.service;

import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.repo.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunRepository runRepository;

    public Run createQueuedRun(String workflowId, String ownerId, Map<String,Object> input) {
        Run r = Run.builder()
                .workflowId(workflowId)
                .ownerId(ownerId)
                .status("queued")
                .createdAt(Instant.now())
                .startTime(null)
                .endTime(null)
                .input(input)
                .attempts(0)
                .build();
        return runRepository.save(r);
    }

    public Run save(Run run) {
        return runRepository.save(run);
    }

    public Run getRun(String runId) {
        return runRepository.findById(runId).orElseThrow(() -> new RuntimeException("Run not found"));
    }

    public List<Run> listRunsForWorkflow(String workflowId) {
        return runRepository.findByWorkflowId(workflowId);
    }

    public List<Run> listRunsForOwner(String ownerId) {
        return runRepository.findByOwnerId(ownerId);
    }
}
