package com.example.notification.domain.metrics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notification.domain.metrics.dto.NotificationMetricsResponse;
import com.example.notification.domain.metrics.service.NotificationMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationMetricsController.class)
class NotificationMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationMetricsService notificationMetricsService;

    @Test
    void getMetricsReturnsExtendedLagFields() throws Exception {
        when(notificationMetricsService.getMetrics()).thenReturn(new NotificationMetricsResponse(
                1L,
                0L,
                10L,
                2L,
                1L,
                11L,
                3L,
                76.92,
                15.34
        ));

        mockMvc.perform(get("/api/v1/notifications/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestQueuedLag").value(11))
                .andExpect(jsonPath("$.data.malformedQueuedLag").value(3))
                .andExpect(jsonPath("$.data.successRate").value(76.92));
    }
}
