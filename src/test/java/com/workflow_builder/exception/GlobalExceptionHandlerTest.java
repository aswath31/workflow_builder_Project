package com.workflow_builder.exception;

import com.workflow_builder.workflow.WorkflowController;
import com.workflow_builder.workflow.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;


import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { WorkflowController.class, GlobalExceptionHandler.class })
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;

    @MockBean
    private com.workflow_builder.auth.JwtService jwtService; // FIX: Mock JwtService for JwtFilter

    @Test
    @WithMockUser
    public void testResourceNotFound() throws Exception {
        when(workflowService.getWorkflow("non-existent"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/workflows/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Workflow not found with id: non-existent"));
    }

    @Test
    @WithMockUser
    public void testValidationFailure() throws Exception {
        com.workflow_builder.workflow.Workflow invalid = com.workflow_builder.workflow.Workflow.builder()
                .name("") // Invalid: Blank name
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/workflows")
                .with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
