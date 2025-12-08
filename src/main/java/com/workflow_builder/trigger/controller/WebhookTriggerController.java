package com.workflow_builder.trigger.controller;

import com.workflow_builder.run.model.Run;
import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.service.TriggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trigger/webhook")
public class WebhookTriggerController {

    private final TriggerService triggerService;

    public WebhookTriggerController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @PostMapping("/{path}")
    public ResponseEntity<?> receiveWebhook(
            @PathVariable String path,
            @RequestBody(required = false) Object body
    ) {

        TriggerConfig config = triggerService.getWebhookTrigger(path);

        if (config == null || !config.isActive()) {
            return ResponseEntity.badRequest()
                    .body("Invalid / inactive webhook trigger");
        }

        // ✅ This now matches TriggerService method
        Run run = triggerService.executeWorkflow(config.getWorkflowId(), body);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Webhook Trigger Executed",
                        "workflowId", config.getWorkflowId(),
                        "runId", run.getId()
                )
        );
    }
}
