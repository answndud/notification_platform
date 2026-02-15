package com.example.notification.domain.request.controller;

import com.example.notification.domain.request.dto.NotificationRequestCreateRequest;
import com.example.notification.domain.request.dto.NotificationRequestResponse;
import com.example.notification.domain.request.service.NotificationRequestService;
import com.example.notification.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
    public ApiResponse<NotificationRequestResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(requestService.get(id));
    }
}
