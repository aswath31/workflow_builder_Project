package com.workflow_builder.run.controller;

import com.workflow_builder.orchestrator.OrchestratorService;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;
    private final OrchestratorService orchestratorService;

    @GetMapping("/{runId}")
    public ResponseEntity<?> getRun(@PathVariable String runId) {
        Run r = runService.getRun(runId);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/{runId}/retry")
public ResponseEntity<?> retryRun(@PathVariable String runId) {

    Run r = runService.getRun(runId);
    r.setStatus("queued");
    r.setStartTime(null);
    r.setEndTime(null);
    r.setAttempts(r.getAttempts() + 1);

    runService.save(r);

    orchestratorService.processRun(r.getId()); // async automatically

    return ResponseEntity.accepted()
            .body(Map.of("runId", r.getId(), "status", "queued"));
}

}
