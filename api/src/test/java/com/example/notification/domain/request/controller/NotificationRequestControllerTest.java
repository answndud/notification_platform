package com.example.notification.domain.request.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.dto.NotificationRequestListResponse;
import com.example.notification.domain.request.dto.NotificationRequestResponse;
import com.example.notification.domain.request.service.NotificationRequestService;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationRequestController.class)
class NotificationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRequestService notificationRequestService;

    @Test
    void createReturnsPriorityInResponse() throws Exception {
        when(notificationRequestService.create(org.mockito.ArgumentMatchers.any(NotificationRequestCreateRequest.class)))
                .thenReturn(new NotificationRequestResponse(
                        11L,
                        "order-11",
                        "HIGH",
                        "QUEUED",
                        LocalDateTime.of(2026, 2, 18, 20, 0)
                ));

        String body = """
                {
                  "requestKey": "order-11",
                  "templateCode": "ORDER_PAID",
                  "receiverIds": [1001],
                  "variables": {"orderNo": "11"},
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void listReturnsPriorityInItems() throws Exception {
        when(notificationRequestService.list("queued", "low", "order-21", 0, 20))
                .thenReturn(new NotificationRequestListResponse(
                        List.of(new NotificationRequestResponse(
                                21L,
                                "order-21",
                                "LOW",
                                "QUEUED",
                                LocalDateTime.of(2026, 2, 18, 20, 1)
                        )),
                        0,
                        20,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/v1/notifications/requests")
                        .param("status", "queued")
                        .param("priority", "low")
                        .param("requestKey", "order-21")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].priority").value("LOW"));
    }

    @Test
    void getReturnsPriorityInResponse() throws Exception {
        when(notificationRequestService.get(31L))
                .thenReturn(new NotificationRequestResponse(
                        31L,
                        "order-31",
                        "NORMAL",
                        "PROCESSING",
                        LocalDateTime.of(2026, 2, 18, 20, 2)
                ));

        mockMvc.perform(get("/api/v1/notifications/requests/{id}", 31L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("NORMAL"));
    }

    @Test
    void getByRequestKeyReturnsPriorityInResponse() throws Exception {
        when(notificationRequestService.getByRequestKey("order-77"))
                .thenReturn(new NotificationRequestResponse(
                        77L,
                        "order-77",
                        "HIGH",
                        "COMPLETED",
                        LocalDateTime.of(2026, 2, 18, 20, 7)
                ));

        mockMvc.perform(get("/api/v1/notifications/requests/by-key")
                        .param("requestKey", "order-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value(77))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void getReturns404WhenRequestMissing() throws Exception {
        when(notificationRequestService.get(404L)).thenThrow(new BusinessException(ErrorCode.REQUEST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/notifications/requests/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N404"));
    }

    @Test
    void getByRequestKeyReturns404WhenRequestMissing() throws Exception {
        when(notificationRequestService.getByRequestKey("missing-key"))
                .thenThrow(new BusinessException(ErrorCode.REQUEST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/notifications/requests/by-key")
                        .param("requestKey", "missing-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N404"));
    }
}
