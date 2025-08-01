package kr.hhplus.be.server.interfaces.dto.request;

import kr.hhplus.be.server.domain.model.CouponType;

import java.time.LocalDateTime;

public record CouponRegisterRequest(
        String name,
        CouponType type,
        Long discountAmount,
        int quantity,
        LocalDateTime expiredAt
) {
}
