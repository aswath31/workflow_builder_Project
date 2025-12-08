package com.workflow_builder.log.controller;

import com.workflow_builder.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/run/{runId}")
    public ResponseEntity<?> getLogs(@PathVariable String runId) {
        return ResponseEntity.ok(logService.getLogsForRun(runId));
    }
}
