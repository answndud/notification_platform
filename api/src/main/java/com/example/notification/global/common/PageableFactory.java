package com.example.notification.global.common;

import com.example.notification.global.exception.BusinessException;
import com.example.notification.global.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableFactory {

    private PageableFactory() {
    }

    public static Pageable of(int page, int size) {
        if (page < ApiPagingPolicy.MIN_PAGE
                || size < ApiPagingPolicy.MIN_SIZE
                || size > ApiPagingPolicy.MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return PageRequest.of(page, size);
    }
}
