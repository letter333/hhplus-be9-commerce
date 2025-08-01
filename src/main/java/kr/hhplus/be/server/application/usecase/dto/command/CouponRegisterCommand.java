package kr.hhplus.be.server.application.usecase.dto.command;

import kr.hhplus.be.server.domain.model.CouponType;

import java.time.LocalDateTime;

public record CouponRegisterCommand(
        String name,
        CouponType type,
        Long discountAmount,
        int quantity,
        LocalDateTime expiredAt
) {
}
