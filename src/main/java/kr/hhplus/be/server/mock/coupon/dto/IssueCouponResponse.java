package kr.hhplus.be.server.mock.coupon.dto;

import java.time.LocalDateTime;

public record IssueCouponResponse(
        Long id,
        String couponName,
        String couponCode,
        CouponResponse.type couponType,
        Long amount,
        Long percentage,
        CouponResponse.status status,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
}
