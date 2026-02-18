package com.example.notification.web;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notification.domain.dlq.controller.DlqController;
import com.example.notification.domain.dlq.service.DlqService;
import com.example.notification.domain.request.controller.NotificationRequestController;
import com.example.notification.domain.request.service.NotificationRequestService;
import com.example.notification.domain.task.controller.DeliveryTaskController;
import com.example.notification.domain.task.service.DeliveryTaskService;
import com.example.notification.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        NotificationRequestController.class,
        DeliveryTaskController.class,
        DlqController.class
})
@Import(GlobalExceptionHandler.class)
class ApiInputValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRequestService notificationRequestService;
    @MockBean
    private DeliveryTaskService deliveryTaskService;
    @MockBean
    private DlqService dlqService;

    @Test
    void requestListReturns400WhenSizeTooLarge() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/requests")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));

        verify(notificationRequestService, never()).list(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void taskRetryReturns400WhenIdInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/tasks/{id}/retry", 0L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));

        verify(deliveryTaskService, never()).retry(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void dlqListReturns400WhenRequestIdNotNumber() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/dlq")
                        .param("requestId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));

        verify(dlqService, never()).getDlqTasks(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void createReturns400WhenPriorityInvalid() throws Exception {
        String body = """
                {
                  "requestKey": "order-1",
                  "templateCode": "ORDER_PAID",
                  "receiverIds": [1001],
                  "variables": {"orderNo": "1"},
                  "priority": "URGENT"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));

        verify(notificationRequestService, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createReturns400WhenReceiverIdNegative() throws Exception {
        String body = """
                {
                  "requestKey": "order-1",
                  "templateCode": "ORDER_PAID",
                  "receiverIds": [-1],
                  "variables": {"orderNo": "1"},
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));

        verify(notificationRequestService, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void taskListReturns400WhenPriorityInvalid() throws Exception {
        org.mockito.Mockito.when(deliveryTaskService.list(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()
        )).thenThrow(new com.example.notification.global.exception.BusinessException(
                com.example.notification.global.exception.ErrorCode.INVALID_INPUT
        ));

        mockMvc.perform(get("/api/v1/notifications/tasks")
                        .param("priority", "urgent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N400"));
    }
}
