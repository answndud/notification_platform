package com.example.notification.global.common;

import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;

public final class ApiInputNormalizer {

    private static final int REQUEST_KEY_MAX_LENGTH = 120;
    private static final int TEMPLATE_CODE_MAX_LENGTH = 80;

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

    public static <E extends Enum<E>> E normalizeOptionalEnum(String value, Class<E> enumType) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static String normalizeRequiredRequestKey(String requestKey) {
        return normalizeRequiredText(requestKey, REQUEST_KEY_MAX_LENGTH);
    }

    public static String normalizeOptionalRequestKey(String requestKey) {
        return normalizeOptionalText(requestKey, REQUEST_KEY_MAX_LENGTH);
    }

    public static String escapeLikePattern(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static String normalizeRequiredTemplateCode(String templateCode) {
        return normalizeRequiredText(templateCode, TEMPLATE_CODE_MAX_LENGTH);
    }

    private static String normalizeRequiredText(String value, int maxLength) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return normalized;
    }

    private static String normalizePriority(String priority) {
        String normalized = priority.trim().toUpperCase();
        return switch (normalized) {
            case "HIGH", "NORMAL", "LOW" -> normalized;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}
