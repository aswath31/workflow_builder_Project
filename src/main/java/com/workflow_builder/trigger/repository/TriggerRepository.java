package com.workflow_builder.trigger.repository;

import com.workflow_builder.trigger.model.TriggerConfig;
import com.workflow_builder.trigger.model.TriggerType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TriggerRepository extends MongoRepository<TriggerConfig, String> {

    List<TriggerConfig> findByTriggerTypeAndActiveTrue(TriggerType type);

    TriggerConfig findByWebhookPath(String webhookPath);
}
