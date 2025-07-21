package kr.hhplus.be.server.mock.coupon.dto;

import java.time.ZonedDateTime;

public record CouponResponse(
        Long id,
        String couponName,
        String couponCode,
        type couponType,
        Long amount,
        Long percentage,
        status status,
        ZonedDateTime usedAt,
        ZonedDateTime expiredAt,
        ZonedDateTime createdAt
) {
    public enum type {
        AMOUNT, PERCENTAGE
    }

    public enum status {
        ISSUED, USED, RESERVED, EXPIRED
    }
}
