package com.workflow_builder.trigger.service;

import com.workflow_builder.orchestrator.OrchestratorService;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.service.RunService;
import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.repository.TriggerRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TriggerService {

    private final TriggerRepository triggerRepository;
    private final RunService runService;
    private final OrchestratorService orchestratorService;

    public TriggerService(
            TriggerRepository triggerRepository,
            RunService runService,
            OrchestratorService orchestratorService
    ) {
        this.triggerRepository = triggerRepository;
        this.runService = runService;
        this.orchestratorService = orchestratorService;
    }

    public TriggerConfig createTrigger(TriggerConfig config) {
        return triggerRepository.save(config);
    }

    public TriggerConfig getWebhookTrigger(String path) {
        return triggerRepository.findByWebhookPath(path);
    }

    // ✅ USED BY WEBHOOK
    public Run executeWorkflow(String workflowId, Object payload) {

        Run run = runService.createQueuedRun(
                workflowId,
                "SYSTEM",
                Map.of("source", "webhook", "payload", payload)
        );

        orchestratorService.processRun(run.getId());

        return run;
    }

    // ✅ USED BY CRON
    public Run executeWorkflow(String workflowId) {

        Run run = runService.createQueuedRun(
                workflowId,
                "SYSTEM",
                Map.of("source", "cron")
        );

        orchestratorService.processRun(run.getId());

        return run;
    }
}
