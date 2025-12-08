package com.workflow_builder.trigger.controller;

import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.service.TriggerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trigger")
public class TriggerConfigController {

    private final TriggerService triggerService;

    public TriggerConfigController(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @PostMapping("/create")
    public TriggerConfig createTrigger(@RequestBody TriggerConfig config) {
        return triggerService.createTrigger(config);
    }
}
