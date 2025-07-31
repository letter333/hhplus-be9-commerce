package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.CouponType;

import java.time.LocalDateTime;

public record CouponRegisterResponse(
        Long id,
        String name,
        CouponType type,
        Long discountAmount,
        int quantity,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) {
}
