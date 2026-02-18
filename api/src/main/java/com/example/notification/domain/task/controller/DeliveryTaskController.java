package com.example.notification.domain.task.controller;

import com.example.notification.domain.task.dto.DeliveryTaskListResponse;
import com.example.notification.domain.task.dto.DeliveryTaskRetryResponse;
import com.example.notification.domain.task.service.DeliveryTaskService;
import com.example.notification.global.common.ApiPagingPolicy;
import com.example.notification.global.common.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_PAGE_VALUE) @Min(ApiPagingPolicy.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_SIZE_VALUE)
            @Min(ApiPagingPolicy.MIN_SIZE)
            @Max(ApiPagingPolicy.MAX_SIZE) int size
    ) {
        return ApiResponse.ok(deliveryTaskService.list(requestId, channel, status, priority, page, size));
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<DeliveryTaskRetryResponse> retry(@PathVariable @Positive Long id) {
        return ApiResponse.ok(deliveryTaskService.retry(id));
    }
}
