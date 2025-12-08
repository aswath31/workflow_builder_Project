package com.workflow_builder.workflow;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {

    List<Workflow> findByOwnerId(String ownerId);
}
