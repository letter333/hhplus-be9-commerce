package kr.hhplus.be.server.application.usecase.dto.command;

import kr.hhplus.be.server.domain.model.PaymentMethod;

public record PaymentProcessCommand(
        Long userId,
        Long orderId,
        PaymentMethod paymentMethod
) {
}
