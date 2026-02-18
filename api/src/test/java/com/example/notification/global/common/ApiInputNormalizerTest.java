package com.example.notification.global.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.notification.domain.request.entity.NotificationRequestStatus;
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

    @Test
    void normalizeRequiredRequestKeyTrimsValue() {
        assertThat(ApiInputNormalizer.normalizeRequiredRequestKey("  order-1  ")).isEqualTo("order-1");
    }

    @Test
    void normalizeRequiredRequestKeyThrowsWhenTooLong() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeRequiredRequestKey("a".repeat(121)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void normalizeRequiredTemplateCodeThrowsWhenTooLong() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeRequiredTemplateCode("A".repeat(81)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void normalizeOptionalRequestKeyTrimsValue() {
        assertThat(ApiInputNormalizer.normalizeOptionalRequestKey("  order-1  ")).isEqualTo("order-1");
    }

    @Test
    void normalizeOptionalRequestKeyReturnsNullWhenBlank() {
        assertThat(ApiInputNormalizer.normalizeOptionalRequestKey("   ")).isNull();
    }

    @Test
    void normalizeOptionalRequestKeyThrowsWhenTooLong() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeOptionalRequestKey("x".repeat(121)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void escapeLikePatternEscapesWildcardCharacters() {
        assertThat(ApiInputNormalizer.escapeLikePattern("ord%_\\key"))
                .isEqualTo("ord\\%\\_\\\\key");
    }

    @Test
    void normalizeOptionalEnumReturnsEnumValue() {
        NotificationRequestStatus status = ApiInputNormalizer.normalizeOptionalEnum(" queued ", NotificationRequestStatus.class);

        assertThat(status).isEqualTo(NotificationRequestStatus.QUEUED);
    }

    @Test
    void normalizeOptionalEnumThrowsWhenInvalid() {
        assertThatThrownBy(() -> ApiInputNormalizer.normalizeOptionalEnum("done", NotificationRequestStatus.class))
                .isInstanceOf(BusinessException.class);
    }
}
