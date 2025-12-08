package com.workflow_builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;






@SpringBootApplication
@EnableScheduling
public class WorkflowBuilderApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowBuilderApplication.class, args);
    }
}

