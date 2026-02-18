package com.example.notification.domain.dlq.controller;

import com.example.notification.domain.dlq.dto.DlqListResponse;
import com.example.notification.domain.dlq.dto.DlqReplayResponse;
import com.example.notification.domain.dlq.dto.DlqTaskResponse;
import com.example.notification.domain.dlq.service.DlqService;
import com.example.notification.global.common.ApiPagingPolicy;
import com.example.notification.global.common.ApiResponse;
import com.example.notification.global.common.ApiValidationPatterns;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/notifications/dlq")
public class DlqController {

    private final DlqService dlqService;

    public DlqController(DlqService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping
    public ApiResponse<DlqListResponse> list(
            @RequestParam(required = false) @Positive Long requestId,
            @RequestParam(required = false) @Size(max = 120) String requestKey,
            @RequestParam(required = false) @Pattern(regexp = ApiValidationPatterns.CHANNEL) String channel,
            @RequestParam(required = false) @Pattern(regexp = ApiValidationPatterns.PRIORITY) String priority,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_PAGE_VALUE) @Min(ApiPagingPolicy.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_SIZE_VALUE)
            @Min(ApiPagingPolicy.MIN_SIZE)
            @Max(ApiPagingPolicy.MAX_SIZE) int size
    ) {
        return ApiResponse.ok(dlqService.getDlqTasks(requestId, requestKey, channel, priority, page, size));
    }

    @PostMapping("/{id}/replay")
    public ApiResponse<DlqReplayResponse> replay(@PathVariable @Positive Long id) {
        return ApiResponse.ok(dlqService.replay(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<DlqTaskResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(dlqService.get(id));
    }
}
