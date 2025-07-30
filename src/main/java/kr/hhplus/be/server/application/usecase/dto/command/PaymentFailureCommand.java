package kr.hhplus.be.server.application.usecase.dto.command;

import kr.hhplus.be.server.domain.model.PaymentMethod;

public record PaymentFailureCommand(
        Long userId,
        Long orderId,
        PaymentMethod paymentMethod
) {
}
