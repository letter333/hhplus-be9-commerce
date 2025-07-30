package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.CouponType;
import kr.hhplus.be.server.domain.model.UserCouponStatus;

import java.time.LocalDateTime;

public record CouponIssueResponse(
        Long id,
        String couponName,
        String couponCode,
        CouponType couponType,
        Long discountAmount,
        Long discountPercentage,
        UserCouponStatus status,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
}
