package kr.hhplus.be.server.mock.coupon.dto;

import java.time.ZonedDateTime;

public record IssueCouponResponse(
        Long id,
        String couponName,
        String couponCode,
        CouponResponse.type couponType,
        Long amount,
        Long percentage,
        CouponResponse.status status,
        ZonedDateTime expiredAt,
        ZonedDateTime createdAt
) {
}
