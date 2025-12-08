package com.workflow_builder.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow_builder.orchestrator.OrchestratorService;
import com.workflow_builder.run.model.Run;
import com.workflow_builder.run.service.RunService;
import com.workflow_builder.workflow.TriggerDefinition;
import com.workflow_builder.workflow.Workflow;
import com.workflow_builder.workflow.WorkflowRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class WebhookControllerTest {

 
  

@Test
void testReceiveWebhook_Success() throws Exception {
    WorkflowRepository workflowRepository = mock(WorkflowRepository.class);
    RunService runService = mock(RunService.class);
    OrchestratorService orchestratorService = mock(OrchestratorService.class);
    ObjectMapper objectMapper = new ObjectMapper();

    WebhookController controller = new WebhookController(
            workflowRepository,
            runService,
            orchestratorService,
            objectMapper
    );

    Workflow wf = new Workflow();
    wf.setId("WF1");
    wf.setOwnerId("OWNER1");
    wf.setState("PUBLISHED");

    TriggerDefinition trigger = new TriggerDefinition();
    trigger.setType("WEBHOOK");
    trigger.setSecret("my-secret");

    wf.setTriggers(List.of(trigger));

    when(workflowRepository.findById("WF1")).thenReturn(Optional.of(wf));

    byte[] body = "{\"msg\":\"hello\"}".getBytes();

    // ✅ Generate exact signature the same way controller does:
    String expectedSignature = com.workflow_builder.utils.HmacUtils
        .computeHmacSha256Base64("my-secret", body);


    Run dummyRun = new Run();
    dummyRun.setId("RUN123");

    when(runService.createQueuedRun(eq("WF1"), eq("OWNER1"), any()))
            .thenReturn(dummyRun);

    ResponseEntity<?> response = controller.receiveWebhook(
            "WF1",
            body,
            expectedSignature,
            null,
            "application/json"
    );

    assertEquals(202, response.getStatusCodeValue());
}

}
