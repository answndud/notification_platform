package com.example.notification.domain.task.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notification.domain.task.dto.DeliveryTaskListResponse;
import com.example.notification.domain.task.dto.DeliveryTaskResponse;
import com.example.notification.domain.task.service.DeliveryTaskService;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeliveryTaskController.class)
class DeliveryTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryTaskService deliveryTaskService;

    @Test
    void getReturnsPriorityInResponse() throws Exception {
        when(deliveryTaskService.get(15L)).thenReturn(new DeliveryTaskResponse(
                15L,
                3L,
                1001L,
                "EMAIL",
                "HIGH",
                "FAILED",
                1,
                3,
                LocalDateTime.of(2026, 2, 18, 15, 0),
                LocalDateTime.of(2026, 2, 18, 14, 30)
        ));

        mockMvc.perform(get("/api/v1/notifications/tasks/{id}", 15L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void listReturnsPriorityInItems() throws Exception {
        when(deliveryTaskService.list(3L, "order-3", "email", "failed", "high", 0, 20))
                .thenReturn(new DeliveryTaskListResponse(
                        List.of(new DeliveryTaskResponse(
                                15L,
                                3L,
                                1001L,
                                "EMAIL",
                                "HIGH",
                                "FAILED",
                                1,
                                3,
                                LocalDateTime.of(2026, 2, 18, 15, 0),
                                LocalDateTime.of(2026, 2, 18, 14, 30)
                        )),
                        0,
                        20,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/v1/notifications/tasks")
                        .param("requestId", "3")
                        .param("requestKey", "order-3")
                        .param("channel", "email")
                        .param("status", "failed")
                        .param("priority", "high")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].priority").value("HIGH"));
    }

    @Test
    void getReturns404WhenTaskMissing() throws Exception {
        when(deliveryTaskService.get(404L)).thenThrow(new BusinessException(ErrorCode.TASK_NOT_FOUND));

        mockMvc.perform(get("/api/v1/notifications/tasks/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N404_TASK"));
    }

    @Test
    void retryReturns409WhenTaskRetryNotAllowed() throws Exception {
        when(deliveryTaskService.retry(15L)).thenThrow(new BusinessException(ErrorCode.TASK_RETRY_NOT_ALLOWED));

        mockMvc.perform(post("/api/v1/notifications/tasks/{id}/retry", 15L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N409_TASK_RETRY"));
    }
}
