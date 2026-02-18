package com.example.notification.global.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.notification.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

class PageableFactoryTest {

    @Test
    void ofReturnsPageableWhenWithinPolicyRange() {
        var pageable = PageableFactory.of(0, ApiPagingPolicy.MAX_SIZE);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(ApiPagingPolicy.MAX_SIZE);
    }

    @Test
    void ofThrowsWhenPageBelowMinimum() {
        assertThatThrownBy(() -> PageableFactory.of(-1, ApiPagingPolicy.MIN_SIZE))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void ofThrowsWhenSizeOutsidePolicyRange() {
        assertThatThrownBy(() -> PageableFactory.of(0, ApiPagingPolicy.MIN_SIZE - 1))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> PageableFactory.of(0, ApiPagingPolicy.MAX_SIZE + 1))
                .isInstanceOf(BusinessException.class);
    }
}
