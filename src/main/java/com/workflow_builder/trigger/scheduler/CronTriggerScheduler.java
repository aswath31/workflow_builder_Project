package com.workflow_builder.trigger.scheduler;

import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.model.TriggerType;
import com.workflow_builder.trigger.repository.TriggerRepository;
import com.workflow_builder.trigger.service.TriggerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CronTriggerScheduler {

    private final TriggerRepository triggerRepository;
    private final TriggerService triggerService;

    public CronTriggerScheduler(
            TriggerRepository triggerRepository,
            TriggerService triggerService
    ) {
        this.triggerRepository = triggerRepository;
        this.triggerService = triggerService;
    }

    @Scheduled(fixedRate = 60000)
    public void runCronTriggers() {

        List<TriggerConfig> cronTriggers =
                triggerRepository.findByTriggerTypeAndActiveTrue(TriggerType.CRON);

        cronTriggers.forEach(trigger -> {
            try {
                triggerService.executeWorkflow(trigger.getWorkflowId()); // ✅ FIXED
                System.out.println("Cron Trigger Executed: " + trigger.getWorkflowId());
            } catch (Exception e) {
                System.out.println("Cron Trigger Failed: " + e.getMessage());
            }
        });
    }
}
