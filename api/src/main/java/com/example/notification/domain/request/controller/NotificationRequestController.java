package com.example.notification.domain.request.controller;

import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.dto.NotificationRequestListResponse;
import com.example.notification.domain.request.dto.NotificationRequestResponse;
import com.example.notification.domain.request.service.NotificationRequestService;
import com.example.notification.global.common.ApiPagingPolicy;
import com.example.notification.global.common.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/notifications/requests")
public class NotificationRequestController {

    private final NotificationRequestService requestService;

    public NotificationRequestController(NotificationRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ApiResponse<NotificationRequestResponse> create(@Valid @RequestBody NotificationRequestCreateRequest request) {
        return ApiResponse.ok(requestService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationRequestResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(requestService.get(id));
    }

    @GetMapping
    public ApiResponse<NotificationRequestListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_PAGE_VALUE) @Min(ApiPagingPolicy.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiPagingPolicy.DEFAULT_SIZE_VALUE)
            @Min(ApiPagingPolicy.MIN_SIZE)
            @Max(ApiPagingPolicy.MAX_SIZE) int size
    ) {
        return ApiResponse.ok(requestService.list(status, page, size));
    }
}
