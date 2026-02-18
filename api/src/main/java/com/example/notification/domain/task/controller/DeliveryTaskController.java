package com.example.notification.domain.task.controller;

import com.example.notification.domain.task.dto.DeliveryTaskListResponse;
import com.example.notification.domain.task.dto.DeliveryTaskResponse;
import com.example.notification.domain.task.dto.DeliveryTaskRetryResponse;
import com.example.notification.domain.task.service.DeliveryTaskService;
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
@RequestMapping("/api/v1/notifications/tasks")
public class DeliveryTaskController {

    private final DeliveryTaskService deliveryTaskService;

    public DeliveryTaskController(DeliveryTaskService deliveryTaskService) {
        this.deliveryTaskService = deliveryTaskService;
    }

    @GetMapping
    public ApiResponse<DeliveryTaskListResponse> list(
            @RequestParam(required = false) @Positive Long requestId,
            @RequestParam(required = false) @Size(max = 120) String requestKey,
            @RequestParam(required = false) @Pattern(regexp = ApiValidationPatterns.CHANNEL) String channel,
            @RequestParam(required = false) @Pattern(regexp = ApiValidationPatterns.TASK_STATUS) String status,
            @RequestParam(required = false) @Pattern(regexp = ApiValidationPatterns.PRIORITY) String priority,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_PAGE_VALUE) @Min(ApiPagingPolicy.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_SIZE_VALUE)
            @Min(ApiPagingPolicy.MIN_SIZE)
            @Max(ApiPagingPolicy.MAX_SIZE) int size
    ) {
        return ApiResponse.ok(deliveryTaskService.list(requestId, requestKey, channel, status, priority, page, size));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<DeliveryTaskRetryResponse> retry(@PathVariable @Positive Long id) {
        return ApiResponse.ok(deliveryTaskService.retry(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeliveryTaskResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(deliveryTaskService.get(id));
    }
}
