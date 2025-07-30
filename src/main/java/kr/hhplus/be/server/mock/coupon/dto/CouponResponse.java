package kr.hhplus.be.server.mock.coupon.dto;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String couponName,
        String couponCode,
        type couponType,
        Long amount,
        Long percentage,
        status status,
        LocalDateTime usedAt,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
    public enum type {
        AMOUNT, PERCENTAGE
    }

    public enum status {
        ISSUED, USED, RESERVED, EXPIRED
    }
}
