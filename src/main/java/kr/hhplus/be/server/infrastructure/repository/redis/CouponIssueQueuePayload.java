package kr.hhplus.be.server.infrastructure.repository.redis;

import java.time.LocalDateTime;

public record CouponIssueQueuePayload(
        Long couponId,
        Long userId,
        String couponCode,
        LocalDateTime expiredAt,
        int retryCount
) {
}
