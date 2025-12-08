package com.workflow_builder.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.service.RunService;
import com.workflow_builder.utils.HmacUtils;
import com.workflow_builder.workflow.TriggerDefinition;
import com.workflow_builder.workflow.Workflow;
import com.workflow_builder.workflow.WorkflowRepository;
import com.workflow_builder.orchestrator.OrchestratorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WorkflowRepository workflowRepository;
    private final RunService runService;
    private final OrchestratorService orchestratorService;
    private final ObjectMapper objectMapper;

    @PostMapping("/{workflowId}")
    public ResponseEntity<?> receiveWebhook(
            @PathVariable String workflowId,
            @RequestBody(required = false) byte[] bodyBytes,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signatureHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestHeader(value = "Content-Type", required = false) String contentType
    ) throws Exception {

        if (bodyBytes == null) bodyBytes = new byte[0];

        Workflow wf = workflowRepository.findById(workflowId).orElse(null);
        if (wf == null || !"PUBLISHED".equalsIgnoreCase(wf.getState())) {
            return ResponseEntity.badRequest().body(Map.of("error", "workflow not found or not published"));
        }

        Optional<TriggerDefinition> webhookTrigger = wf.getTriggers() == null ? Optional.empty() :
                wf.getTriggers().stream().filter(t -> "WEBHOOK".equalsIgnoreCase(t.getType())).findFirst();

        if (webhookTrigger.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "no webhook trigger configured for workflow"));
        }

        String secret = webhookTrigger.get().getSecret();
        if (secret == null || secret.isBlank()) {
            return ResponseEntity.status(500).body(Map.of("error", "webhook secret not configured"));
        }

        // Compute expected HMAC
        String expected = HmacUtils.computeHmacSha256Base64(secret, bodyBytes);

        boolean ok = false;

        if (signatureHeader != null) {
            ok = HmacUtils.constantTimeEquals(expected, signatureHeader);
        } else if (tokenParam != null) {
            ok = HmacUtils.constantTimeEquals(secret, tokenParam);
        }

        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid webhook signature"));
        }

        // Parse body JSON or raw
        Object bodyObj;
        if (contentType != null && contentType.contains("application/json")) {
            bodyObj = objectMapper.readValue(bodyBytes, Map.class);
        } else {
            bodyObj = new String(bodyBytes, StandardCharsets.UTF_8);
        }

        // Create run
        Run run = runService.createQueuedRun(
                wf.getId(),
                wf.getOwnerId(),
                Map.of("webhookPayload", bodyObj)
        );

        // Async execution
        CompletableFuture.runAsync(() -> {
            try {
                orchestratorService.processRun(run.getId());
            } catch (Exception e) {
                log.error("Error processing webhook run async", e);
            }
        });

        return ResponseEntity.accepted().body(
                Map.of("runId", run.getId(), "status", "queued")
        );
    }
}
