package com.example.notification.global.common;

import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;

public final class ApiInputNormalizer {

    private ApiInputNormalizer() {
    }

    public static String normalizeRequiredPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return normalizePriority(priority);
    }

    public static String normalizeOptionalPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        return normalizePriority(priority);
    }

    public static String normalizeOptionalChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return null;
        }
        String normalized = channel.trim().toUpperCase();
        return switch (normalized) {
            case "EMAIL" -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }

    private static String normalizePriority(String priority) {
        String normalized = priority.trim().toUpperCase();
        return switch (normalized) {
            case "HIGH", "NORMAL", "LOW" -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}
