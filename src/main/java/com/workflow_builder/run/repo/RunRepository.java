package com.workflow_builder.run.repo;

import com.workflow_builder.run.model.Run;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RunRepository extends MongoRepository<Run, String> {
    List<Run> findByWorkflowId(String workflowId);
    List<Run> findByOwnerId(String ownerId);
}
