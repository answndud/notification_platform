package com.example.notification.global.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.notification.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

class ApiInputNormalizerTest {

    @Test
    void normalizeOptionalPriorityReturnsUppercase() {
        assertThat(ApiInputNormalizer.normalizeOptionalPriority(" low ")).isEqualTo("LOW");
    }

    @Test
    void normalizeOptionalPriorityReturnsNullWhenBlank() {
        assertThat(ApiInputNormalizer.normalizeOptionalPriority(" ")).isNull();
    }

    @Test
    void normalizeRequiredPriorityThrowsWhenBlank() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeRequiredPriority(""))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void normalizeOptionalChannelReturnsUppercase() {
        assertThat(ApiInputNormalizer.normalizeOptionalChannel(" email ")).isEqualTo("EMAIL");
    }

    @Test
    void normalizeOptionalChannelThrowsWhenUnsupported() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeOptionalChannel("sms"))
                .isInstanceOf(BusinessException.class);
    }
}
