package com.example.notification.domain.dlq.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notification.domain.dlq.dto.DlqListResponse;
import com.example.notification.domain.dlq.dto.DlqTaskResponse;
import com.example.notification.domain.dlq.service.DlqService;
import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DlqController.class)
class DlqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DlqService dlqService;

    @Test
    void getReturnsPriorityAndLastResult() throws Exception {
        when(dlqService.get(19L)).thenReturn(new DlqTaskResponse(
                19L,
                5L,
                1001L,
                "EMAIL",
                "HIGH",
                "DLQ",
                3,
                3,
                LocalDateTime.of(2026, 2, 18, 16, 5),
                "FORCED_FAIL",
                "Forced failure",
                LocalDateTime.of(2026, 2, 18, 16, 0)
        ));

        mockMvc.perform(get("/api/v1/notifications/dlq/{id}", 19L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("DLQ"))
                .andExpect(jsonPath("$.data.lastResultCode").value("FORCED_FAIL"));
    }

    @Test
    void listReturnsPriorityInItems() throws Exception {
        when(dlqService.getDlqTasks(5L, "order-5", "email", "high", 0, 20))
                .thenReturn(new DlqListResponse(
                        List.of(new DlqTaskResponse(
                                19L,
                                5L,
                                1001L,
                                "EMAIL",
                                "HIGH",
                                "DLQ",
                                3,
                                3,
                                LocalDateTime.of(2026, 2, 18, 16, 5),
                                "FORCED_FAIL",
                                "Forced failure",
                                LocalDateTime.of(2026, 2, 18, 16, 0)
                        )),
                        0,
                        20,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/v1/notifications/dlq")
                        .param("requestId", "5")
                        .param("requestKey", "order-5")
                        .param("channel", "email")
                        .param("priority", "high")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].priority").value("HIGH"));
    }

    @Test
    void getReturns404WhenDlqTaskMissing() throws Exception {
        when(dlqService.get(404L)).thenThrow(new BusinessException(ErrorCode.DLQ_TASK_NOT_FOUND));

        mockMvc.perform(get("/api/v1/notifications/dlq/{id}", 404L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N404_DLQ"));
    }

    @Test
    void replayReturns409WhenReplayNotAllowed() throws Exception {
        when(dlqService.replay(19L)).thenThrow(new BusinessException(ErrorCode.DLQ_REPLAY_NOT_ALLOWED));

        mockMvc.perform(post("/api/v1/notifications/dlq/{id}/replay", 19L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("N409_DLQ_REPLAY"));
    }
}
