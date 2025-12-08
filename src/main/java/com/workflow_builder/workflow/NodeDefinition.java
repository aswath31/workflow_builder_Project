package com.workflow_builder.workflow;


import java.util.Map;



import lombok.Data;
import java.util.List;


@Data
public class NodeDefinition {
    private String id;
    private String type;
    private Map<String,Object> config;
    private List<String> next;   // <- ensure this is a List
    private String onTrue;
    private String onFalse;

    // If earlier code relies on 'getNextRaw', add a compatibility getter:
    public Object getNextRaw() {
        return next;
    }
}

