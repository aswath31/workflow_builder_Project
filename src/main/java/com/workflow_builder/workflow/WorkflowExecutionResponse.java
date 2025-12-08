package com.workflow_builder.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowExecutionResponse {
    private String workflowId;
    private String status;
    private Object output;
}

