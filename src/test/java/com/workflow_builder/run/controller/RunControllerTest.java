package com.workflow_builder.run.controller;

import com.workflow_builder.orchestrator.OrchestratorService;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.service.RunService;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RunControllerTest {

    @Test
    void testRetryRun() {

        RunService runService = mock(RunService.class);
        OrchestratorService orchestratorService = mock(OrchestratorService.class);

        RunController controller = new RunController(runService, orchestratorService);

        Run run = new Run();
        run.setId("RUN123");
        run.setStatus("failed");
        run.setAttempts(0);

        when(runService.getRun("RUN123")).thenReturn(run);

        ResponseEntity<?> response = controller.retryRun("RUN123");

        assertEquals(202, response.getStatusCodeValue());
        assertEquals("queued", run.getStatus());
        assertEquals(1, run.getAttempts());

        verify(runService, times(1)).save(run);
        verify(orchestratorService, times(1)).processRun("RUN123");

        // It's called inside new Thread(), so we only check "no direct call"
    }
}
