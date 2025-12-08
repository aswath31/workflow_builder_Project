package com.workflow_builder.trigger.scheduler;

import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.model.TriggerType;
import com.workflow_builder.trigger.repository.TriggerRepository;
import com.workflow_builder.trigger.service.TriggerService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class CronTriggerSchedulerTest {

    @Test
    void testRunCronTriggers() {

        TriggerRepository repo = mock(TriggerRepository.class);
        TriggerService service = mock(TriggerService.class);

        CronTriggerScheduler scheduler = new CronTriggerScheduler(repo, service);

        TriggerConfig cfg = new TriggerConfig();
        cfg.setWorkflowId("WF-200");

        when(repo.findByTriggerTypeAndActiveTrue(TriggerType.CRON))
                .thenReturn(List.of(cfg));

        scheduler.runCronTriggers();

        verify(service, times(1)).executeWorkflow("WF-200");
    }
}
